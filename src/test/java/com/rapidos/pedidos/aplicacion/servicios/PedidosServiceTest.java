package com.rapidos.pedidos.aplicacion.servicios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.rapidos.pedidos.ConfirmacionMedida;
import com.rapidos.pedidos.PedidoConfirmado;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoComando;
import com.rapidos.pedidos.aplicacion.puertos.salida.Cobro;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuPort;
import com.rapidos.pedidos.aplicacion.puertos.salida.PedidoRepository;
import com.rapidos.pedidos.aplicacion.puertos.salida.PublicadorDeEventos;
import com.rapidos.pedidos.testsupport.RelojDePrueba;
import com.rapidos.pedidos.dominio.menu.ItemMenu;
import com.rapidos.pedidos.dominio.pedido.LineaPedido;
import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

class PedidosServiceTest {

    private final MenuFalso menu = new MenuFalso();
    private final PedidosEnMemoria repositorio = new PedidosEnMemoria();
    private final CobroFalso cobro = new CobroFalso();
    private final EventosEnMemoria eventos = new EventosEnMemoria();
    private final CrearPedidoService servicio =
            new CrearPedidoService(menu, repositorio, cobro, eventos, new RelojDePrueba());

    @Test
    void confirmaCobraGuardaYPublica() {
        menu.agregar(new ItemMenu(1L, 1L, "Arepa", new BigDecimal("12000.00")));
        menu.agregar(new ItemMenu(2L, 1L, "Jugo", new BigDecimal("5000.00")));

        var resumen = servicio.crear(comando(new LineaPedido(1L, 2), new LineaPedido(2L, 1)), "llave-1");

        assertThat(resumen.total()).isEqualByComparingTo("29000.00");
        assertThat(resumen.estado()).isEqualTo("CONFIRMADO");
        assertThat(cobro.veces).isEqualTo(1);
        assertThat(repositorio.lineas).hasSize(2);
        assertThat(eventos.publicados).hasSize(2);
        assertThat(eventos.publicados.get(0)).isEqualTo(new PedidoConfirmado(resumen.id(), 1L, resumen.total()));
        assertThat(eventos.publicados.get(1)).isInstanceOf(ConfirmacionMedida.class);
    }

    @Test
    void laMismaLlaveNoCobraNiPublicaOtraVez() {
        menu.agregar(new ItemMenu(1L, 1L, "Arepa", new BigDecimal("12000.00")));
        var comando = comando(new LineaPedido(1L, 2));

        var primero = servicio.crear(comando, "llave-1");
        var segundo = servicio.crear(comando, "llave-1");

        assertThat(segundo.id()).isEqualTo(primero.id());
        assertThat(cobro.veces).isEqualTo(1);
        assertThat(eventos.publicados).hasSize(2);
    }

    @Test
    void unCobroRechazadoNoGuardaNiPublica() {
        menu.agregar(new ItemMenu(1L, 1L, "Arepa", new BigDecimal("12000.00")));
        cobro.acepta = false;

        assertThatThrownBy(() -> servicio.crear(comando(new LineaPedido(1L, 1)), "llave-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cobro rechazado");
        assertThat(repositorio.porId).isEmpty();
        assertThat(eventos.publicados).isEmpty();
    }

    private static CrearPedidoComando comando(LineaPedido... lineas) {
        return new CrearPedidoComando(1L, 10L, List.of(lineas));
    }

    private static final class MenuFalso implements MenuPort {
        private boolean activo = true;
        private final Map<Long, ItemMenu> items = new LinkedHashMap<>();

        void agregar(ItemMenu item) {
            items.put(item.id(), item);
        }

        @Override
        public boolean restauranteActivo(Long restauranteId) {
            return activo;
        }

        @Override
        public Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId) {
            var item = items.get(itemId);
            if (item == null || !restauranteId.equals(item.restauranteId())) {
                return Optional.empty();
            }
            return Optional.of(item);
        }
    }

    private static final class PedidosEnMemoria implements PedidoRepository {
        private long secuencia = 1;
        private final Map<String, PedidoResumen> porLlave = new LinkedHashMap<>();
        final Map<Long, PedidoResumen> porId = new LinkedHashMap<>();
        final List<LineaPedido> lineas = new ArrayList<>();

        @Override
        public Optional<PedidoResumen> porLlave(String llave) {
            return Optional.ofNullable(porLlave.get(llave));
        }

        @Override
        public Optional<PedidoResumen> porId(Long pedidoId) {
            return Optional.ofNullable(porId.get(pedidoId));
        }

        @Override
        public PedidoResumen insertar(
                Long restauranteId,
                Long clienteId,
                BigDecimal total,
                String estado,
                Instant creadoEn,
                String llavePedido) {
            var resumen = new PedidoResumen(secuencia++, restauranteId, clienteId, total, estado, creadoEn);
            porLlave.put(llavePedido, resumen);
            porId.put(resumen.id(), resumen);
            return resumen;
        }

        @Override
        public void insertarLinea(Long pedidoId, Long itemId, int cantidad, BigDecimal precioUnitario) {
            lineas.add(new LineaPedido(itemId, cantidad));
        }
    }

    private static final class CobroFalso implements Cobro {
        private boolean acepta = true;
        private int veces;

        @Override
        public boolean cobrar(Long clienteId, BigDecimal total) {
            veces++;
            return acepta;
        }
    }

    private static final class EventosEnMemoria implements PublicadorDeEventos {
        private final List<Object> publicados = new ArrayList<>();

        @Override
        public void publicar(Object evento) {
            publicados.add(evento);
        }
    }
}
