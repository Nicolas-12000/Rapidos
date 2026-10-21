package com.rapidos.despacho.aplicacion.servicios;

import com.rapidos.despacho.ConsultaRepartidor;
import com.rapidos.despacho.aplicacion.puertos.entrada.ProcesarPedidoConfirmado;
import com.rapidos.despacho.aplicacion.puertos.entrada.RegistrarEstadoUseCase;
import com.rapidos.despacho.dominio.EstadoEntrega;
import com.rapidos.despacho.aplicacion.puertos.salida.Avisador;
import com.rapidos.pedidos.PedidoConfirmado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class DespachoFlujoIT {

    private static final long PEDIDO_ID = 900_003L;

    @Autowired
    ProcesarPedidoConfirmado confirmar;

    @Autowired
    RegistrarEstadoUseCase estados;

    @Autowired
    ConsultaRepartidor consulta;

    @Autowired
    JdbcClient jdbc;

    @MockitoBean
    Avisador avisador;

    private PedidoConfirmado pedido;

    @BeforeEach
    void prepararPedido() {
        limpiar();
        jdbc.sql("""
                        INSERT INTO pedido
                            (id, restaurante_id, cliente_id, total, estado, creado_en, llave_pedido)
                        VALUES
                            (:id, 1, 10, 12000.00, 'CONFIRMADO', :creadoEn, :llave)
                        """)
                .param("id", PEDIDO_ID)
                .param("creadoEn", Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")))
                .param("llave", "despacho-it-" + PEDIDO_ID)
                .update();
        pedido = new PedidoConfirmado(PEDIDO_ID, 1L, new BigDecimal("12000.00"));
        reset(avisador);
    }

    @AfterEach
    void limpiarDespues() {
        limpiar();
    }

    @Test
    void unFalloRevierteElWorkerYElReintentoProcesaUnaSolaVez() {
        doThrow(new IllegalStateException("fallo simulado"))
                .doNothing()
                .when(avisador).avisarRestaurante(any(PedidoConfirmado.class));

        assertThatThrownBy(() -> confirmar.procesar(pedido))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("fallo simulado");
        assertThat(consulta.repartidorDe(PEDIDO_ID)).isEmpty();

        confirmar.procesar(pedido);
        confirmar.procesar(pedido); // entrega duplicada del mismo evento

        assertThat(consulta.repartidorDe(PEDIDO_ID)).contains(3L);
        assertThat(estados.estadoDe(PEDIDO_ID)).contains(EstadoEntrega.ASIGNADO);
        assertThat(cantidad("despacho_pedido")).isEqualTo(1);
        verify(avisador, times(2)).avisarRestaurante(pedido);
        verify(avisador, times(1)).avisarRepartidor(3L, pedido);
    }

    @Test
    void laMismaLlaveDeEstadoNoSeProcesaDosVeces() {
        confirmar.procesar(pedido);

        estados.registrarEstado(3L, PEDIDO_ID, EstadoEntrega.RECOGIDO, "estado-unico");
        estados.registrarEstado(3L, PEDIDO_ID, EstadoEntrega.ENTREGADO, "estado-unico");

        assertThat(estados.estadoDe(PEDIDO_ID)).contains(EstadoEntrega.RECOGIDO);
        assertThat(cantidad("despacho_estado")).isEqualTo(1);
    }

    private long cantidad(String tabla) {
        String sql = "SELECT COUNT(*) FROM " + tabla + " WHERE pedido_id = :pedidoId";
        return jdbc.sql(sql)
                .param("pedidoId", PEDIDO_ID)
                .query(Long.class)
                .single();
    }

    private void limpiar() {
        jdbc.sql("DELETE FROM despacho_estado WHERE pedido_id = :pedidoId")
                .param("pedidoId", PEDIDO_ID)
                .update();
        jdbc.sql("DELETE FROM despacho_pedido WHERE pedido_id = :pedidoId")
                .param("pedidoId", PEDIDO_ID)
                .update();
        jdbc.sql("DELETE FROM pedido_linea WHERE pedido_id = :pedidoId")
                .param("pedidoId", PEDIDO_ID)
                .update();
        jdbc.sql("DELETE FROM pedido WHERE id = :pedidoId")
                .param("pedidoId", PEDIDO_ID)
                .update();
    }
}
