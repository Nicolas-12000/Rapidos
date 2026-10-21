package com.rapidos.pedidos.infraestructura.entrada;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CrearPedidoRequest(
        @NotNull Long restauranteId,
        @NotNull Long clienteId,
        @NotEmpty @Valid List<LineaPedidoRequest> items) {
}
