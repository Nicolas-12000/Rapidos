package com.rapidos.ubicacion.aplicacion.servicios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rapidos.ubicacion.aplicacion.puertos.salida.RepartidorDePedido;
import com.rapidos.ubicacion.dominio.ReglaDegradacionGps;
import com.rapidos.ubicacion.dominio.UbicacionActual;
import com.rapidos.ubicacion.infraestructura.salida.UbicacionEnMemoria;

class UbicacionServiceTest {

    private final RepartidorDePedido despacho = mock(RepartidorDePedido.class);
    private final ReglaDegradacionGps regla = new ReglaDegradacionGps(20, 5);
    private final RelojFalso reloj = new RelojFalso();
    private final UbicacionEnMemoria almacen = new UbicacionEnMemoria(30, reloj);
    private final UbicacionService servicio = new UbicacionService(despacho, regla, almacen, reloj);

    @BeforeEach
    void asignacion() {
        when(despacho.repartidorDe(7L)).thenReturn(java.util.Optional.of(3L));
    }

    @Test
    void soloLaUltimaPosicionGana() {
        servicio.reportar(3L, 4.1, -74.1);
        servicio.reportar(3L, 4.2, -74.2);

        assertThat(servicio.ultimaDePedido(7L))
                .contains(new UbicacionActual(3L, 4.2, -74.2, reloj.instant()));
    }

    @Test
    void laPosicionVenceTrasElTtl() {
        servicio.reportar(3L, 4.1, -74.1);
        reloj.avanzar(Duration.ofSeconds(29));
        assertThat(servicio.ultimaDePedido(7L)).isPresent();

        reloj.avanzar(Duration.ofSeconds(2));
        assertThat(servicio.ultimaDePedido(7L)).isEmpty();
    }

    @Test
    void pedidoSinRepartidorDevuelveVacio() {
        when(despacho.repartidorDe(8L)).thenReturn(java.util.Optional.empty());

        assertThat(servicio.ultimaDePedido(8L)).isEmpty();
    }

    private static final class RelojFalso extends Clock {
        private Instant ahora = Instant.parse("2026-10-15T00:00:00Z");

        void avanzar(Duration duracion) {
            ahora = ahora.plus(duracion);
        }

        @Override
        public Instant instant() {
            return ahora;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }
}
