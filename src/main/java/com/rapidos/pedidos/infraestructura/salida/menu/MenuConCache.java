package com.rapidos.pedidos.infraestructura.salida.menu;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.rapidos.pedidos.aplicacion.puertos.salida.MenuRepository;
import com.rapidos.pedidos.dominio.menu.ItemMenu;

@Component
@Primary
class MenuConCache implements MenuRepository {

    private final MenuRepository origen;
    private final CacheConTtl<Long, List<ItemMenu>> menus;
    private final CacheConTtl<Long, Boolean> activos;

    @Autowired
    MenuConCache(MenuJdbc jdbc, @Value("${rapidos.menu.cache-ttl:5m}") Duration ttl) {
        this.origen = jdbc;
        this.menus = new CacheConTtl<>(ttl, Clock.systemUTC());
        this.activos = new CacheConTtl<>(ttl, Clock.systemUTC());
    }

    /** Solo pruebas: evita un segundo constructor que Spring confunde con el bean @Primary. */
    static MenuConCache paraPruebas(MenuRepository origen, Duration ttl, Clock clock) {
        return new MenuConCache(origen, ttl, clock);
    }

    private MenuConCache(MenuRepository origen, Duration ttl, Clock clock) {
        this.origen = origen;
        this.menus = new CacheConTtl<>(ttl, clock);
        this.activos = new CacheConTtl<>(ttl, clock);
    }

    @Override
    public List<ItemMenu> menuDe(Long restauranteId) {
        return menus.obtener(restauranteId, origen::menuDe);
    }

    @Override
    public boolean restauranteActivo(Long restauranteId) {
        return activos.obtener(restauranteId, origen::restauranteActivo);
    }
}
