package com.rapidos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.rapidos.despacho.aplicacion.puertos.salida.Avisador;
import com.rapidos.despacho.aplicacion.puertos.salida.DespachoRepository;
import com.rapidos.despacho.aplicacion.servicios.DespachoService;
import com.rapidos.despacho.dominio.EstadoEntrega;
import com.rapidos.pedidos.PedidoConfirmado;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoComando;
import com.rapidos.pedidos.aplicacion.puertos.salida.Cobro;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuPort;
import com.rapidos.pedidos.aplicacion.puertos.salida.PedidoRepository;
import com.rapidos.pedidos.aplicacion.puertos.salida.PublicadorDeEventos;
import com.rapidos.pedidos.aplicacion.servicios.CrearPedidoService;
import com.rapidos.pedidos.testsupport.RelojDePrueba;
import com.rapidos.pedidos.dominio.menu.ItemMenu;
import com.rapidos.pedidos.dominio.pedido.LineaPedido;
import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

/** Pasos 2 a 4 de RAMAS: POST vía MenuPort, evento, listener de despacho. Sin Spring. */
class PedidoFlujoTest {

    private final MenuFalso menu = new MenuFalso();
    private final PedidosEnMemoria pedidos = new PedidosEnMemoria();
    private final EventosEnMemoria eventos = new EventosEnMemoria();
    private final DespachoEnMemoria despachoRepo = new DespachoEnMemoria();
    private final AvisosEnMemoria avisos = new AvisosEnMemoria();
    private final CrearPedidoService crear =
            new CrearPedidoService(menu, pedidos, new CobroOk(), eventos, new RelojDePrueba());
    private final DespachoService despacho =
            new DespachoService(despachoRepo, avisos, () -> RelojDePrueba.INSTANTE_FIJO);

    @Test
    void confirmaPublicaYDespachoAsignaUnaSolaVez() {
        menu.agregar(new ItemMenu(1L, 1L, "Arepa", new BigDecimal("12000.00")));
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(1L, 2)));

        var primero = crear.crear(comando, "pedido-flujo-001");
        var segundo = crear.crear(comando, "pedido-flujo-001");

        assertThat(segundo.id()).isEqualTo(primero.id());
        assertThat(primero.total()).isEqualByComparingTo("24000.00");
        assertThat(eventos.deTipo(PedidoConfirmado.class)).hasSize(1);

        var evento = (PedidoConfirmado) eventos.deTipo(PedidoConfirmado.class).getFirst();
        despacho.procesar(evento);
        despacho.procesar(evento);

        assertThat(despacho.repartidorDe(primero.id())).isPresent();
        assertThat(despacho.estadoDe(primero.id())).contains(EstadoEntrega.ASIGNADO);
        assertThat(avisos.restaurantes).isEqualTo(1);
        assertThat(avisos.repartidores).isEqualTo(1);
    }

    private static final class MenuFalso implements MenuPort {
        private final Map<Long, ItemMenu> items = new LinkedHashMap<>();

        void agregar(ItemMenu item) {
            items.put(item.id(), item);
        }

        @Override
        public boolean restauranteActivo(Long restauranteId) {
            return true;
        }

        @Override
        public Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId) {
            return Optional.ofNullable(items.get(itemId))
                    .filter(item -> restauranteId.equals(item.restauranteId()));
        }
    }

    private static final class PedidosEnMemoria implements PedidoRepository {
        private long secuencia = 1;
        private final Map<String, PedidoResumen> porLlave = new LinkedHashMap<>();
        private final Map<Long, PedidoResumen> porId = new LinkedHashMap<>();

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
        }
    }

    private static final class CobroOk implements Cobro {
        @Override
        public boolean cobrar(Long clienteId, BigDecimal total) {
            return true;
        }
    }

    private static final class EventosEnMemoria implements PublicadorDeEventos {
        private final List<Object> publicados = new ArrayList<>();

        @Override
        public void publicar(Object evento) {
            publicados.add(evento);
        }

        List<Object> deTipo(Class<?> tipo) {
            return publicados.stream().filter(tipo::isInstance).toList();
        }
    }

    private static final class DespachoEnMemoria implements DespachoRepository {
        private final Map<Long, Long> asignaciones = new LinkedHashMap<>();
        private final Map<Long, EstadoEntrega> estados = new LinkedHashMap<>();

        @Override
        public boolean crearAsignacion(Long pedidoId, Long repartidorId, Instant asignadoEn) {
            if (asignaciones.containsKey(pedidoId)) {
                return false;
            }
            asignaciones.put(pedidoId, repartidorId);
            estados.put(pedidoId, EstadoEntrega.ASIGNADO);
            return true;
        }

        @Override
        public boolean guardarEstado(
                Long pedidoId, Long repartidorId, EstadoEntrega estado, String llave, Instant registradoEn) {
            return true;
        }

        @Override
        public void actualizarEstado(Long pedidoId, EstadoEntrega estado, Instant actualizadoEn) {
            estados.put(pedidoId, estado);
        }

        @Override
        public Optional<Long> repartidorDe(Long pedidoId) {
            return Optional.ofNullable(asignaciones.get(pedidoId));
        }

        @Override
        public Optional<EstadoEntrega> estadoDe(Long pedidoId) {
            return Optional.ofNullable(estados.get(pedidoId));
        }
    }

    private static final class AvisosEnMemoria implements Avisador {
        private int restaurantes;
        private int repartidores;

        @Override
        public void avisarRestaurante(PedidoConfirmado pedido) {
            restaurantes++;
        }

        @Override
        public void avisarRepartidor(Long repartidorId, PedidoConfirmado pedido) {
            repartidores++;
        }
    }
}
