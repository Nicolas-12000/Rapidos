package com.rapidos.pedidos.infraestructura.entrada;

import java.math.BigDecimal;

import com.rapidos.pedidos.dominio.menu.ItemMenu;

record ItemMenuResponse(
        Long id,
        Long restauranteId,
        String nombre,
        BigDecimal precio,
        boolean disponible) {

    static ItemMenuResponse de(ItemMenu item) {
        return new ItemMenuResponse(
                item.id(),
                item.restauranteId(),
                item.nombre(),
                item.precio(),
                item.disponible());
    }
}
