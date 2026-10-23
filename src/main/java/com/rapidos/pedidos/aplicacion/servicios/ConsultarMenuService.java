package com.rapidos.pedidos.aplicacion.servicios;

import java.util.List;
import java.util.Optional;

import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarMenuUseCase;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuRepository;
import com.rapidos.pedidos.dominio.menu.ItemMenu;

public class ConsultarMenuService implements ConsultarMenuUseCase {

    private final MenuRepository menu;

    public ConsultarMenuService(MenuRepository menu) {
        this.menu = menu;
    }

    @Override
    public boolean restauranteActivo(Long restauranteId) {
        if (restauranteId == null) {
            return false;
        }
        return menu.restauranteActivo(restauranteId);
    }

    @Override
    public List<ItemMenu> menu(Long restauranteId) {
        if (restauranteId == null) {
            return List.of();
        }
        return List.copyOf(menu.menuDe(restauranteId));
    }

    @Override
    public Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId) {
        if (restauranteId == null || itemId == null) {
            return Optional.empty();
        }
        return menu(restauranteId).stream()
                .filter(item -> item.Id().equals(itemId))
                .findFirst();
    }
}
