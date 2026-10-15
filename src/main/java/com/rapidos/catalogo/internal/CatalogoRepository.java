package com.rapidos.catalogo.internal;

import com.rapidos.catalogo.ItemMenu;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a las tablas restaurante e item_menu. Privado del módulo catalogo.
 */
@Repository
class CatalogoRepository {

    /** Convierte cada fila de item_menu en un ItemMenu. */
    private static final RowMapper<ItemMenu> FILA_A_ITEM = (fila, numero) -> new ItemMenu(
            fila.getLong("id"),
            fila.getLong("restaurante_id"),
            fila.getString("nombre"),
            fila.getBigDecimal("precio"),
            fila.getBoolean("disponible"));

    private final JdbcClient jdbc;

    CatalogoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    List<ItemMenu> menuDe(Long restauranteId) {
        return jdbc.sql("""
                        SELECT id, restaurante_id, nombre, precio, disponible
                        FROM item_menu
                        WHERE restaurante_id = :restauranteId
                        ORDER BY id
                        """)
                .param("restauranteId", restauranteId)
                .query(FILA_A_ITEM)
                .list();
    }

    boolean restauranteActivo(Long restauranteId) {
        return jdbc.sql("""
                        SELECT EXISTS (
                            SELECT 1 FROM restaurante
                            WHERE id = :restauranteId AND activo
                        )
                        """)
                .param("restauranteId", restauranteId)
                .query(Boolean.class)
                .single();
    }
}