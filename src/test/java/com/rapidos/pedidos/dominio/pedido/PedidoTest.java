package com.rapidos.pedidos.dominio.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class PedidoTest {

    @Test
    void elTotalSaleDeLasLineas() {
        var pedido = Pedido.confirmar(
                1L,
                10L,
                List.of(new LineaPedido(1L, 2), new LineaPedido(2L, 1)),
                List.of(new BigDecimal("12000.00"), new BigDecimal("5000.00")));

        assertThat(pedido.total()).isEqualByComparingTo("29000.00");
    }

    @Test
    void rechazaCantidadMenorQueUno() {
        assertThatThrownBy(() -> new LineaPedido(1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cantidad >= 1");
    }

    @Test
    void exigeItemIdEnCadaLinea() {
        assertThatThrownBy(() -> new LineaPedido(null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("itemId");
    }

    @Test
    void rechazaUnPedidoSinLineas() {
        assertThatThrownBy(() -> Pedido.confirmar(1L, 10L, List.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un ítem");
    }

    @Test
    void exigeRestauranteYCliente() {
        assertThatThrownBy(() -> Pedido.confirmar(null, 10L, List.of(new LineaPedido(1L, 1)), List.of(new BigDecimal("1000"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorios");
    }
}
