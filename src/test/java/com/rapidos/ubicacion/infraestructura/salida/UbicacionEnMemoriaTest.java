package com.rapidos.ubicacion.infraestructura.salida;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.rapidos.support.ClockAvanzable;
import com.rapidos.ubicacion.dominio.UbicacionActual;

class UbicacionEnMemoriaTest {

    private final ClockAvanzable reloj = new ClockAvanzable(Instant.parse("2026-10-15T00:00:00Z"));
    private final UbicacionEnMemoria almacen = new UbicacionEnMemoria(30, reloj);

    @Test
    void laPosicionVenceTrasElTtl() {
        almacen.guardar(new UbicacionActual(3L, 4.1, -74.1, reloj.instant()));
        reloj.avanzar(Duration.ofSeconds(29));
        assertThat(almacen.ultimaDe(3L)).isPresent();

        reloj.avanzar(Duration.ofSeconds(2));
        assertThat(almacen.ultimaDe(3L)).isEmpty();
    }
}
