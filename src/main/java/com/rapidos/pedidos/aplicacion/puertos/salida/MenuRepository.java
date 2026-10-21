package com.rapidos.pedidos.aplicacion.puertos.salida;

import java.util.List;

import com.rapidos.pedidos.dominio.menu.ItemMenu;

public interface MenuRepository {

    List<ItemMenu> menuDe(Long restauranteId);

    boolean restauranteActivo(Long restauranteId);
}
