package com.rapidos.pedidos.internal;

import com.rapidos.catalogo.CatalogoApi;
import com.rapidos.pedidos.CrearPedidoComando;
import com.rapidos.pedidos.LineaPedido;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PedidosServiceValidacionTest {

    private final CatalogoApi catalogo = mock(CatalogoApi.class);

    @Test
    void rechazaCantidadMenorQueUno() {
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(1L, 0)));

        assertThatThrownBy(() -> PedidosService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cantidad >= 1");
    }

    @Test
    void rechazaListaDeItemsVacia() {
        var comando = new CrearPedidoComando(1L, 10L, List.of());

        assertThatThrownBy(() -> PedidosService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un ítem");
    }

    @Test
    void rechazaListaDeItemsNula() {
        var comando = new CrearPedidoComando(1L, 10L, null);

        assertThatThrownBy(() -> PedidosService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un ítem");
    }

    @Test
    void exigeItemIdEnCadaLinea() {
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(null, 1)));

        assertThatThrownBy(() -> PedidosService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("itemId");
    }

    @Test
    void exigeRestauranteYCliente() {
        var comando = new CrearPedidoComando(null, 10L, List.of(new LineaPedido(1L, 1)));

        assertThatThrownBy(() -> PedidosService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void rechazaUnItemQueNoExisteEnElCatalogo() {
        when(catalogo.obtenerItem(1L, 99L)).thenReturn(Optional.empty());
        var comando = new CrearPedidoComando(1L, 10L, List.of(new LineaPedido(99L, 1)));

        assertThatThrownBy(() -> PedidosService.calcularTotal(comando, catalogo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ítem no válido: 99");
    }
}
