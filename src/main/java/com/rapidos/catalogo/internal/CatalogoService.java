package com.rapidos.catalogo.internal;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rapidos.catalogo.CatalogoApi;
import com.rapidos.catalogo.ItemMenu;

/**
 * Implementación de CatalogoApi con caché en memoria (ADR-001).
 * Privada del módulo: los demás solo ven la interfaz CatalogoApi.
 */
@Service
class CatalogoService implements CatalogoApi {

    private static final Logger log = LoggerFactory.getLogger(CatalogoService.class);

    private final CatalogoRepository repositorio;
    private final CacheConTtl<Long, List<ItemMenu>> menus;
    private final CacheConTtl<Long, Boolean> activos;

    /** Constructor que usa Spring. El TTL se puede cambiar en application.yml (por defecto 5 min). */
    @Autowired
    CatalogoService(CatalogoRepository repositorio,
                    @Value("${rapidos.catalogo.cache-ttl:5m}") Duration ttl) {
        this(repositorio, ttl, Clock.systemUTC());
    }

    /** Constructor para pruebas: permite controlar el reloj. */
    CatalogoService(CatalogoRepository repositorio, Duration ttl, Clock clock) {
        this.repositorio = repositorio;
        this.menus = new CacheConTtl<>(ttl, clock);
        this.activos = new CacheConTtl<>(ttl, clock);
    }

    @Override
    public boolean restauranteActivo(Long restauranteId) {
        if (restauranteId == null) {
            return false;
        }
        return activos.obtener(restauranteId, repositorio::restauranteActivo);
    }

    @Override
    public List<ItemMenu> menu(Long restauranteId) {
        if (restauranteId == null) {
            return List.of();
        }
        return menus.obtener(restauranteId, this::cargarMenu);
    }

    @Override
    public Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId) {
        if (restauranteId == null || itemId == null) {
            return Optional.empty();
        }
        // Sale de la caché: validar un pedido en el pico no toca la base de datos
        return menu(restauranteId).stream()
                .filter(item -> item.id().equals(itemId))
                .findFirst();
    }

    /** Solo se ejecuta cuando el menú no está en caché o ya venció. */
    private List<ItemMenu> cargarMenu(Long restauranteId) {
        log.info("Menú del restaurante {} cargado desde PostgreSQL (no estaba en caché)", restauranteId);
        return List.copyOf(repositorio.menuDe(restauranteId));
    }
}