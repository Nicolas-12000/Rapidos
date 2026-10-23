package com.rapidos.pedidos.dominio.menu;

import java.math.BigDecimal;

public record ItemMenu(
        Long id,
        Long restauranteId,
        String nombre,
        BigDecimal precio,
        boolean disponible) {

    /** Compatibilidad con pruebas que asumen el plato disponible. */
    public ItemMenu(Long id, Long restauranteId, String nombre, BigDecimal precio) {
        this(id, restauranteId, nombre, precio, true);
    }
}
