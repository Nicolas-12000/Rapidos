package com.rapidos.pedidos.infraestructura.salida.menu;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.rapidos.pedidos.aplicacion.puertos.salida.MenuRepository;
import com.rapidos.pedidos.dominio.menu.ItemMenu;

@Component
@Primary
public class MenuConCache implements MenuRepository {

    private final MenuRepository origen;
    private final CacheConTtl<Long, List<ItemMenu>> menus;
    private final CacheConTtl<Long, Boolean> activos;

    public MenuConCache(
            MenuJdbc jdbc,
            @Value("${rapidos.catalogo.cache-ttl:5m}") Duration ttl) {
        this(jdbc, ttl, Clock.systemUTC());
    }

    public MenuConCache(MenuRepository origen, Duration ttl, Clock clock) {
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
