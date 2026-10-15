package com.rapidos.catalogo.internal;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rapidos.catalogo.CatalogoApi;
import com.rapidos.catalogo.ItemMenu;

/**
 * Implementación de CatalogoApi. Privada del módulo:
 * los demás módulos solo ven la interfaz CatalogoApi.
 */
@Service
class CatalogoService implements CatalogoApi {

    private final CatalogoRepository repositorio;

    CatalogoService(CatalogoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public boolean restauranteActivo(Long restauranteId) {
        if (restauranteId == null) {
            return false;
        }
        return repositorio.restauranteActivo(restauranteId);
    }

    @Override
    public List<ItemMenu> menu(Long restauranteId) {
        if (restauranteId == null) {
            return List.of();
        }
        // List.copyOf: lista inmutable, nadie puede modificarla por accidente
        return List.copyOf(repositorio.menuDe(restauranteId));
    }

    @Override
    public Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId) {
        if (restauranteId == null || itemId == null) {
            return Optional.empty();
        }
        // Busca dentro del menú de ESE restaurante: un plato de otro restaurante no aparece
        return menu(restauranteId).stream()
                .filter(item -> item.id().equals(itemId))
                .findFirst();
    }
}