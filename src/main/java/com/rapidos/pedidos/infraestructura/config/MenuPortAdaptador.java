package com.rapidos.pedidos.infraestructura.config;

import java.util.Optional;

import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarMenuUseCase;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuPort;
import com.rapidos.pedidos.dominio.menu.ItemMenu;

/** Adapta el caso de uso de menú al puerto que usa el orquestador de pedidos. */
class MenuPortAdaptador implements MenuPort {

    private final ConsultarMenuUseCase menu;

    MenuPortAdaptador(ConsultarMenuUseCase menu) {
        this.menu = menu;
    }

    @Override
    public boolean restauranteActivo(Long restauranteId) {
        return menu.restauranteActivo(restauranteId);
    }

    @Override
    public Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId) {
        return menu.obtenerItem(restauranteId, itemId);
    }
}
