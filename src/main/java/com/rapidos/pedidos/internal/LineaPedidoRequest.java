package com.rapidos.pedidos.internal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LineaPedidoRequest(
        @NotNull Long itemId,
        @Min(1) int cantidad) {
}
