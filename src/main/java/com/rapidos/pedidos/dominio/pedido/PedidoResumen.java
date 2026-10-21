package com.rapidos.pedidos.dominio.pedido;

import java.math.BigDecimal;
import java.time.Instant;

public record PedidoResumen(Long id, Long restauranteId, Long clienteId,
                            BigDecimal total, String estado, Instant creadoEn) {
}
