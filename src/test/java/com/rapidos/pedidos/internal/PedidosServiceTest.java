package com.rapidos.pedidos.internal;

import com.rapidos.catalogo.CatalogoApi;
import com.rapidos.catalogo.ItemMenu;
import com.rapidos.pedidos.CrearPedidoComando;
import com.rapidos.pedidos.LineaPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PedidosServiceTest {

    @Test
    void calculaElTotalDelPedido() {
        var catalogo = mock(CatalogoApi.class);
        when(catalogo.obtenerItem(1L, 1L))
                .thenReturn(Optional.of(new ItemMenu(1L, 1L, "Arepa", new BigDecimal("12000.00"))));
        when(catalogo.obtenerItem(1L, 2L))
                .thenReturn(Optional.of(new ItemMenu(2L, 1L, "Jugo", new BigDecimal("5000.00"))));

        var comando = new CrearPedidoComando(
                1L,
                10L,
                List.of(new LineaPedido(1L, 2), new LineaPedido(2L, 1)));

        var total = PedidosService.calcularTotal(comando, catalogo);

        assertThat(total).isEqualByComparingTo("29000.00");
    }
}
