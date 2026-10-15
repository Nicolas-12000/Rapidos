package com.rapidos.catalogo;

import java.util.List;
import java.util.Optional;

/** API pública del módulo catalogo. Dueño: Dev 2. */
/**
 * API pública del módulo catalogo. Dueño: Dev 2.
 *
 * <p>Los menús se sirven desde una caché en memoria (ADR-001), así que llamar
 * a estos métodos en el pico de las 7:00 p. m. no golpea la base de datos.
 * Es el único punto de entrada al módulo: nadie debe importar
 * {@code com.rapidos.catalogo.internal}.
 */
public interface CatalogoApi {

    /**
     * Indica si el restaurante existe y está activo.
     *
     * @return {@code false} si no existe, está inactivo o el id es {@code null}
     */
    boolean restauranteActivo(Long restauranteId);

    /**
     * Menú completo del restaurante, incluidos los platos no disponibles.
     *
     * @return lista inmutable; vacía si el restaurante no existe. Nunca {@code null}
     */
    List<ItemMenu> menu(Long restauranteId);

    /**
     * Busca un plato dentro del menú de un restaurante.
     * Quien lo use (pedidos) debe revisar además {@link ItemMenu#disponible()}.
     *
     * @return vacío si el plato no existe o pertenece a otro restaurante
     */
    Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId);
}
