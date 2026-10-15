package com.rapidos.despacho.internal;

import com.rapidos.despacho.EstadoEntrega;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record RegistrarEstadoRequest(
        @NotNull Long pedidoId,
        @NotNull EstadoEntrega estado,
        @NotBlank String llave) {
}
