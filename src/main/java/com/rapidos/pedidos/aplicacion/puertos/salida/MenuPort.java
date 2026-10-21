package com.rapidos.pedidos.aplicacion.puertos.salida;

import java.util.Optional;

import com.rapidos.pedidos.dominio.menu.ItemMenu;

/** Lo que el orquestador le pide al menú. No pasa por HTTP. */
public interface MenuPort {

    boolean restauranteActivo(Long restauranteId);

    Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId);
}
