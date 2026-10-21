package com.rapidos.pedidos.dominio.pedido;

import java.math.BigDecimal;
import java.util.List;

/** Pedido confirmable. El total sale de las líneas; no lo calcula el servicio. */
public record Pedido(
        Long restauranteId,
        Long clienteId,
        List<LineaPedido> lineas,
        List<BigDecimal> preciosUnitarios,
        BigDecimal total) {

    public Pedido {
        if (restauranteId == null || clienteId == null) {
            throw new IllegalArgumentException("restauranteId y clienteId son obligatorios");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un ítem");
        }
        if (preciosUnitarios == null || preciosUnitarios.size() != lineas.size()) {
            throw new IllegalArgumentException("Cada línea requiere precio");
        }
        BigDecimal suma = BigDecimal.ZERO;
        for (int i = 0; i < lineas.size(); i++) {
            suma = suma.add(lineas.get(i).subtotal(preciosUnitarios.get(i)));
        }
        if (total == null || total.compareTo(suma) != 0) {
            throw new IllegalArgumentException("El total no coincide con las líneas");
        }
        lineas = List.copyOf(lineas);
        preciosUnitarios = List.copyOf(preciosUnitarios);
    }

    public static Pedido confirmar(
            Long restauranteId,
            Long clienteId,
            List<LineaPedido> lineas,
            List<BigDecimal> preciosUnitarios) {
        BigDecimal total = BigDecimal.ZERO;
        if (lineas != null && preciosUnitarios != null && lineas.size() == preciosUnitarios.size()) {
            for (int i = 0; i < lineas.size(); i++) {
                total = total.add(lineas.get(i).subtotal(preciosUnitarios.get(i)));
            }
        }
        return new Pedido(restauranteId, clienteId, lineas, preciosUnitarios, total);
    }
}
