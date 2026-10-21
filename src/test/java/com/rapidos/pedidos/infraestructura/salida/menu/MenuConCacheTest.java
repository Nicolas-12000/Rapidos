package com.rapidos.pedidos.infraestructura.salida.menu;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rapidos.pedidos.aplicacion.puertos.salida.MenuRepository;
import com.rapidos.pedidos.aplicacion.servicios.ConsultarMenuService;
import com.rapidos.pedidos.dominio.menu.ItemMenu;

class MenuConCacheTest {

    private final MenuRepository repositorio = mock(MenuRepository.class);
    private final RelojFalso reloj = new RelojFalso();
    private final ConsultarMenuService servicio = new ConsultarMenuService(
            new MenuConCache(repositorio, Duration.ofMinutes(5), reloj));

    @BeforeEach
    void datos() {
        when(repositorio.menuDe(1L)).thenReturn(List.of(
                new ItemMenu(1L, 1L, "Arepa reina pepiada", new BigDecimal("12000"), true),
                new ItemMenu(2L, 1L, "Jugo natural", new BigDecimal("5000"), true)));
        when(repositorio.restauranteActivo(1L)).thenReturn(true);
    }

    @Test
    void elMenuSeConsultaUnaSolaVezMientrasEsteVigente() {
        servicio.menu(1L);
        servicio.menu(1L);
        servicio.menu(1L);

        verify(repositorio, times(1)).menuDe(1L);
    }

    @Test
    void validarPlatosTambienSaleDeLaCache() {
        // Así valida pedidos Dev 1: un obtenerItem por cada plato del pedido
        assertThat(servicio.obtenerItem(1L, 1L)).isPresent();
        assertThat(servicio.obtenerItem(1L, 2L)).isPresent();
        assertThat(servicio.obtenerItem(1L, 99L)).isEmpty();

        verify(repositorio, times(1)).menuDe(1L);
    }

    @Test
    void restauranteActivoTambienSeGuardaEnCache() {
        servicio.restauranteActivo(1L);
        servicio.restauranteActivo(1L);

        verify(repositorio, times(1)).restauranteActivo(1L);
    }

    @Test
    void alVencerElTtlVuelveAConsultarLaBase() {
        servicio.menu(1L);
        reloj.avanzar(Duration.ofMinutes(6));
        servicio.menu(1L);

        verify(repositorio, times(2)).menuDe(1L);
    }
}