package com.rapidos.catalogo;

import java.util.List;
import java.util.Optional;

/** API pública del módulo catalogo. Dueño: Dev 2. */
public interface CatalogoApi {

    boolean restauranteActivo(Long restauranteId);

    List<ItemMenu> menu(Long restauranteId);

    Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId);
}
