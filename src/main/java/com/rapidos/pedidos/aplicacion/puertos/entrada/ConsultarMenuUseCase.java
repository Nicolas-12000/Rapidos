package com.rapidos.pedidos.aplicacion.puertos.entrada;

import java.util.List;
import java.util.Optional;

import com.rapidos.pedidos.dominio.menu.ItemMenu;

/** Caso de uso del cliente que mira el menú antes de pedir. Dueño: Dev 2. */
public interface ConsultarMenuUseCase {

    boolean restauranteActivo(Long restauranteId);

    List<ItemMenu> menu(Long restauranteId);

    Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId);
}
