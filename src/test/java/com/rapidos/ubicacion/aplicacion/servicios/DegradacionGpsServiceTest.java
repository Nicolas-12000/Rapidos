package com.rapidos.ubicacion.aplicacion.servicios;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.rapidos.ubicacion.dominio.ReglaDegradacionGps;

class DegradacionGpsServiceTest {

    private final ReglaDegradacionGps regla = new ReglaDegradacionGps(5, 3);
    private final DegradacionGpsService servicio = new DegradacionGpsService(regla);

    @Test
    void registraMedidasEnLaRegla() {
        servicio.registrar(100);
        servicio.registrar(100);
        servicio.registrar(100);
        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(5);
    }

    @Test
    void ignoraMedidasNegativas() {
        servicio.registrar(-1);
        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(5);
    }
}
