package com.rapidos.catalogo;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;

/**
 * Prueba el módulo catalogo contra PostgreSQL real
 * (requiere docker compose up -d). Usa los datos de data.sql.
 */
@ApplicationModuleTest
class CatalogoApiTest {

    @Autowired
    CatalogoApi catalogo;

    @Test
    void menuDelRestaurante1TieneTresPlatos() {
        var menu = catalogo.menu(1L);

        assertThat(menu).hasSize(3);
        assertThat(menu.get(0).nombre()).isEqualTo("Arepa reina pepiada");
    }

    @Test
    void menuDeRestauranteInexistenteEstaVacio() {
        assertThat(catalogo.menu(999L)).isEmpty();
        assertThat(catalogo.menu(null)).isEmpty();
    }

    @Test
    void obtieneUnPlatoConSuPrecio() {
        var arepa = catalogo.obtenerItem(1L, 1L);

        assertThat(arepa).isPresent();
        // compareTo y no equals: 12000 y 12000.00 son el mismo precio
        assertThat(arepa.get().precio()).isEqualByComparingTo(new BigDecimal("12000"));
        assertThat(arepa.get().disponible()).isTrue();
    }

    @Test
    void platoAgotadoApareceComoNoDisponible() {
        var agotado = catalogo.obtenerItem(1L, 3L);

        assertThat(agotado).isPresent();
        assertThat(agotado.get().disponible()).isFalse();
    }

    @Test
    void noDevuelvePlatoDeOtroRestaurante() {
        // El plato 4 es de la pizzería (restaurante 2)
        assertThat(catalogo.obtenerItem(1L, 4L)).isEmpty();
    }

    @Test
    void restauranteActivo() {
        assertThat(catalogo.restauranteActivo(1L)).isTrue();
        assertThat(catalogo.restauranteActivo(3L)).isFalse();   // inactivo
        assertThat(catalogo.restauranteActivo(999L)).isFalse(); // no existe
        assertThat(catalogo.restauranteActivo(null)).isFalse();
    }
}