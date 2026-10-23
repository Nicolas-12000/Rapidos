package com.rapidos.pedidos.infraestructura.entrada;

import java.math.BigDecimal;
import java.time.Instant;

import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

record PedidoResumenResponse(
        Long id,
        Long restauranteId,
        Long clienteId,
        BigDecimal total,
        String estado,
        Instant creadoEn) {

    static PedidoResumenResponse de(PedidoResumen resumen) {
        return new PedidoResumenResponse(
                resumen.id(),
                resumen.restauranteId(),
                resumen.clienteId(),
                resumen.total(),
                resumen.estado(),
                resumen.creadoEn());
    }
}
