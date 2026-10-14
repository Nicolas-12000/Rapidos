package com.rapidos.pedidos;

import java.math.BigDecimal;

/** Evento: la "cola de pedidos" de la demo (ADR-001). Lo escucha despacho. */
public record PedidoConfirmado(Long pedidoId, Long restauranteId, BigDecimal total) {
}
