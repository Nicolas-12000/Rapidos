package com.rapidos.pedidos;

/** Evento: cuánto tardó confirmar un pedido. Lo escucha ubicacion para degradar el GPS (ADR-002). */
public record ConfirmacionMedida(long duracionMs) {
}
