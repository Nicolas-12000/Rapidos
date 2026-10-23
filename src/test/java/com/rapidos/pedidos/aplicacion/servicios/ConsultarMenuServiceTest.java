package com.rapidos.pedidos.aplicacion.servicios;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.rapidos.pedidos.aplicacion.puertos.salida.MenuRepository;
import com.rapidos.pedidos.dominio.menu.ItemMenu;

class ConsultarMenuServiceTest {

    private final MenuFalso repositorio = new MenuFalso();
    private final ConsultarMenuService servicio = new ConsultarMenuService(repositorio);

    @Test
    void menuVacioSiElRestauranteNoTienePlatos() {
        repositorio.activo = true;

        assertThat(servicio.menu(1L)).isEmpty();
    }

    @Test
    void noDevuelvePlatoDeOtroRestaurante() {
        repositorio.activo = true;
        repositorio.platos.put(2L, List.of(
                new ItemMenu(4L, 2L, "Pizza", new BigDecimal("32000.00"))));

        assertThat(servicio.obtenerItem(1L, 4L)).isEmpty();
        assertThat(servicio.menu(1L)).isEmpty();
    }

    @Test
    void restauranteInactivo() {
        repositorio.activo = false;
        repositorio.platos.put(1L, List.of(
                new ItemMenu(1L, 1L, "Arepa", new BigDecimal("12000.00"))));

        assertThat(servicio.restauranteActivo(1L)).isFalse();
        assertThat(servicio.menu(1L)).hasSize(1);
        assertThat(servicio.menuSiActivo(1L)).isEmpty();
    }

    private static final class MenuFalso implements MenuRepository {
        private boolean activo;
        private final Map<Long, List<ItemMenu>> platos = new java.util.HashMap<>();

        @Override
        public List<ItemMenu> menuDe(Long restauranteId) {
            return platos.getOrDefault(restauranteId, List.of());
        }

        @Override
        public boolean restauranteActivo(Long restauranteId) {
            return activo && restauranteId != null;
        }
    }
}
