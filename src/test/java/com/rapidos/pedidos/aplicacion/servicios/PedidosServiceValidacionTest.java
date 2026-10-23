package com.rapidos.pedidos.aplicacion.servicios;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoComando;
import com.rapidos.pedidos.aplicacion.puertos.salida.Cobro;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuPort;
import com.rapidos.pedidos.aplicacion.puertos.salida.PedidoRepository;
import com.rapidos.pedidos.testsupport.RelojDePrueba;
import com.rapidos.pedidos.dominio.menu.ItemMenu;
import com.rapidos.pedidos.dominio.pedido.LineaPedido;
import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

class PedidosServiceValidacionTest {

    private final MenuFijo menu = new MenuFijo();
    private final CrearPedidoService servicio = new CrearPedidoService(
            menu, new PedidosVacios(), new CobroFijo(), evento -> {}, new RelojDePrueba());

    @Test
    void rechazaListaDeItemsVacia() {
        var comando = new CrearPedidoComando(1L, 10L, List.of());

        assertThatThrownBy(() -> servicio.crear(comando, "llave-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un ítem");
    }

    @Test
    void rechazaListaDeItemsNula() {
        var comando = new CrearPedidoComando(1L, 10L, null);

        assertThatThrownBy(() -> servicio.crear(comando, "llave-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un ítem");
    }

    @Test
    void exigeRestauranteYCliente() {
        var comando = new CrearPedidoComando(null, 10L, List.of(new LineaPedido(1L, 1)));

        assertThatThrownBy(() -> servicio.crear(comando, "llave-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void rechazaUnItemQueNoExisteEnElMenu() {
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(99L, 1)));

        assertThatThrownBy(() -> servicio.crear(comando, "llave-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ítem no válido: 99");
    }

    @Test
    void rechazaUnItemNoDisponible() {
        menu.agotado = true;
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(3L, 1)));

        assertThatThrownBy(() -> servicio.crear(comando, "llave-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ítem no disponible: 3");
    }

    @Test
    void rechazaRestauranteInactivo() {
        menu.activo = false;
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(1L, 1)));

        assertThatThrownBy(() -> servicio.crear(comando, "llave-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Restaurante no disponible");
    }

    private static final class MenuFijo implements MenuPort {
        private boolean activo = true;
        private boolean agotado;

        @Override
        public boolean restauranteActivo(Long restauranteId) {
            return activo;
        }

        @Override
        public Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId) {
            if (Long.valueOf(99L).equals(itemId)) {
                return Optional.empty();
            }
            if (Long.valueOf(3L).equals(itemId)) {
                return Optional.of(new ItemMenu(3L, 1L, "Arepa de chócolo", new BigDecimal("9000.00"), !agotado));
            }
            return Optional.of(new ItemMenu(itemId, restauranteId, "Arepa", new BigDecimal("12000.00")));
        }
    }

    private static final class PedidosVacios implements PedidoRepository {
        @Override
        public Optional<PedidoResumen> porLlave(String llave) {
            return Optional.empty();
        }

        @Override
        public Optional<PedidoResumen> porId(Long pedidoId) {
            return Optional.empty();
        }

        @Override
        public PedidoResumen insertar(
                Long restauranteId,
                Long clienteId,
                BigDecimal total,
                String estado,
                Instant creadoEn,
                String llavePedido) {
            throw new AssertionError("no debía guardar");
        }

        @Override
        public void insertarLinea(Long pedidoId, Long itemId, int cantidad, BigDecimal precioUnitario) {
            throw new AssertionError("no debía guardar la línea");
        }
    }

    private static final class CobroFijo implements Cobro {
        @Override
        public boolean cobrar(Long clienteId, BigDecimal total) {
            throw new AssertionError("no debía cobrar");
        }
    }
}
