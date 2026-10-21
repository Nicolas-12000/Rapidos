package com.rapidos.despacho.infraestructura.entrada;

import com.rapidos.despacho.dominio.EstadoEntrega;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record RegistrarEstadoRequest(
        @NotNull Long pedidoId,
        @NotNull EstadoEntrega estado,
        @NotBlank String llave) {
}
