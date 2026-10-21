package com.rapidos.pedidos.infraestructura.salida.menu;

import com.rapidos.pedidos.aplicacion.puertos.salida.MenuRepository;
import com.rapidos.pedidos.dominio.menu.ItemMenu;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a las tablas restaurante e item_menu. Lo usa la caché del menú.
 */
@Repository
public class MenuJdbc implements MenuRepository {

    /** Convierte cada fila de item_menu en un ItemMenu. */
    private static final RowMapper<ItemMenu> FILA_A_ITEM = (fila, numero) -> new ItemMenu(
            fila.getLong("id"),
            fila.getLong("restaurante_id"),
            fila.getString("nombre"),
            fila.getBigDecimal("precio"),
            fila.getBoolean("disponible"));

    private final JdbcClient jdbc;

    public MenuJdbc(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ItemMenu> menuDe(Long restauranteId) {
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

    @Override
    public boolean restauranteActivo(Long restauranteId) {
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