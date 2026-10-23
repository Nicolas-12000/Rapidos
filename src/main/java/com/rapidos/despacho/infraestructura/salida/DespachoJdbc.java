package com.rapidos.despacho.infraestructura.salida;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.rapidos.despacho.aplicacion.puertos.salida.DespachoRepository;
import com.rapidos.despacho.dominio.EstadoEntrega;

@Repository
class DespachoJdbc implements DespachoRepository {

    private final JdbcClient jdbc;

    public DespachoJdbc(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean crearAsignacion(Long pedidoId, Long repartidorId, Instant asignadoEn) {
        try {
            int filas = jdbc.sql("""
                            INSERT INTO despacho_pedido
                                (pedido_id, repartidor_id, estado, asignado_en, actualizado_en)
                            VALUES
                                (:pedidoId, :repartidorId, :estado, :asignadoEn, :asignadoEn)
                            """)
                    .param("pedidoId", pedidoId)
                    .param("repartidorId", repartidorId)
                    .param("estado", EstadoEntrega.ASIGNADO.name())
                    .param("asignadoEn", Timestamp.from(asignadoEn))
                    .update();
            return filas == 1;
        } catch (DataIntegrityViolationException duplicado) {
            return false;
        }
    }

    @Override
    public boolean guardarEstado(
            Long pedidoId,
            Long repartidorId,
            EstadoEntrega estado,
            String llave,
            Instant registradoEn) {
        try {
            int filas = jdbc.sql("""
                            INSERT INTO despacho_estado
                                (pedido_id, repartidor_id, estado, llave_estado, registrado_en)
                            VALUES
                                (:pedidoId, :repartidorId, :estado, :llave, :registradoEn)
                            """)
                    .param("pedidoId", pedidoId)
                    .param("repartidorId", repartidorId)
                    .param("estado", estado.name())
                    .param("llave", llave)
                    .param("registradoEn", Timestamp.from(registradoEn))
                    .update();
            return filas == 1;
        } catch (DataIntegrityViolationException llaveDuplicada) {
            return false;
        }
    }

    @Override
    public void actualizarEstado(Long pedidoId, EstadoEntrega estado, Instant actualizadoEn) {
        int filas = jdbc.sql("""
                        UPDATE despacho_pedido
                        SET estado = :estado, actualizado_en = :actualizadoEn
                        WHERE pedido_id = :pedidoId
                        """)
                .param("pedidoId", pedidoId)
                .param("estado", estado.name())
                .param("actualizadoEn", Timestamp.from(actualizadoEn))
                .update();
        if (filas != 1) {
            throw new IllegalStateException("El pedido no tiene un despacho asignado");
        }
    }

    @Override
    public Optional<Long> repartidorDe(Long pedidoId) {
        return jdbc.sql("""
                        SELECT repartidor_id
                        FROM despacho_pedido
                        WHERE pedido_id = :pedidoId
                        """)
                .param("pedidoId", pedidoId)
                .query(Long.class)
                .optional();
    }

    @Override
    public Optional<EstadoEntrega> estadoDe(Long pedidoId) {
        return jdbc.sql("""
                        SELECT estado
                        FROM despacho_pedido
                        WHERE pedido_id = :pedidoId
                        """)
                .param("pedidoId", pedidoId)
                .query(String.class)
                .optional()
                .map(EstadoEntrega::valueOf);
    }
}
