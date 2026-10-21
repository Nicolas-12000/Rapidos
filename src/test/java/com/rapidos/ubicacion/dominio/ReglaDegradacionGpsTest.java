package com.rapidos.ubicacion.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReglaDegradacionGpsTest {

    @Test
    void empiezaEnCincoSegundos() {
        assertThat(new ReglaDegradacionGps(20, 5).frecuenciaSugeridaSegundos()).isEqualTo(5);
    }

    @Test
    void conMenosDeCincoMedicionesNoCambia() {
        ReglaDegradacionGps regla = new ReglaDegradacionGps(20, 5);
        registrar(regla, 2000, 4);

        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(5);
    }

    @Test
    void subeA10LuegoA15YNoPasaDe15() {
        ReglaDegradacionGps regla = new ReglaDegradacionGps(20, 5);
        registrar(regla, 2000, 5);
        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(10);

        registrar(regla, 2000, 5);
        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(15);

        registrar(regla, 2000, 20);
        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(15);
    }

    @Test
    void vuelveACincoCuandoElP95EsMenorQueMil() {
        ReglaDegradacionGps regla = new ReglaDegradacionGps(20, 5);
        registrar(regla, 2000, 5);
        registrar(regla, 2000, 5);
        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(15);

        registrar(regla, 900, 5);
        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(5);
    }

    @Test
    void entreMilYMilQuinientosNoOscila() {
        ReglaDegradacionGps regla = new ReglaDegradacionGps(20, 5);
        registrar(regla, 2000, 5);
        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(10);

        registrar(regla, 1200, 5);
        assertThat(regla.frecuenciaSugeridaSegundos()).isEqualTo(10);
    }

    private static void registrar(ReglaDegradacionGps regla, long duracion, int veces) {
        for (int i = 0; i < veces; i++) {
            regla.registrar(duracion);
        }
    }
}
