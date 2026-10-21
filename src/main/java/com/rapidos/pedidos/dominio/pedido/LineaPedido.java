package com.rapidos.pedidos.dominio.pedido;

import java.math.BigDecimal;

public record LineaPedido(Long itemId, int cantidad) {

    public LineaPedido {
        if (itemId == null || cantidad < 1) {
            throw new IllegalArgumentException("Cada línea requiere itemId y cantidad >= 1");
        }
    }

    public BigDecimal subtotal(BigDecimal precioUnitario) {
        if (precioUnitario == null) {
            throw new IllegalArgumentException("Cada línea requiere precio");
        }
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
