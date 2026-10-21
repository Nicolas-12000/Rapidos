package com.rapidos.pedidos.aplicacion.servicios;

import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoComando;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuPort;
import com.rapidos.pedidos.dominio.menu.ItemMenu;
import com.rapidos.pedidos.dominio.pedido.LineaPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PedidosServiceValidacionTest {

    private final MenuPort catalogo = mock(MenuPort.class);

    @Test
    void rechazaCantidadMenorQueUno() {
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(1L, 0)));

        assertThatThrownBy(() -> CrearPedidoService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cantidad >= 1");
    }

    @Test
    void rechazaListaDeItemsVacia() {
        var comando = new CrearPedidoComando(1L, 10L, List.of());

        assertThatThrownBy(() -> CrearPedidoService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un ítem");
    }

    @Test
    void rechazaListaDeItemsNula() {
        var comando = new CrearPedidoComando(1L, 10L, null);

        assertThatThrownBy(() -> CrearPedidoService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un ítem");
    }

    @Test
    void exigeItemIdEnCadaLinea() {
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(null, 1)));

        assertThatThrownBy(() -> CrearPedidoService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("itemId");
    }

    @Test
    void exigeRestauranteYCliente() {
        var comando = new CrearPedidoComando(null, 10L, List.of(new LineaPedido(1L, 1)));

        assertThatThrownBy(() -> CrearPedidoService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void rechazaUnItemQueNoExisteEnElCatalogo() {
        when(catalogo.obtenerItem(1L, 99L)).thenReturn(Optional.empty());
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(99L, 1)));

        assertThatThrownBy(() -> CrearPedidoService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ítem no válido: 99");
    }

    @Test
    void rechazaUnItemNoDisponible() {
        var agotado = new ItemMenu(3L, 1L, "Arepa de chócolo", new BigDecimal("9000.00"), false);
        when(catalogo.obtenerItem(1L, 3L)).thenReturn(Optional.of(agotado));
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(3L, 1)));

        assertThatThrownBy(() -> CrearPedidoService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ítem no disponible: 3");
    }
}
