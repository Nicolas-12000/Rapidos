package com.rapidos.ubicacion.aplicacion.servicios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rapidos.ubicacion.aplicacion.puertos.salida.Reloj;
import com.rapidos.ubicacion.aplicacion.puertos.salida.RepartidorDePedido;
import com.rapidos.ubicacion.aplicacion.puertos.salida.UbicacionStore;
import com.rapidos.ubicacion.dominio.ReglaDegradacionGps;
import com.rapidos.ubicacion.dominio.UbicacionActual;

class UbicacionServiceTest {

    private final RepartidorDePedido despacho = mock(RepartidorDePedido.class);
    private final ReglaDegradacionGps regla = new ReglaDegradacionGps(20, 5);
    private final Reloj reloj = () -> Instant.parse("2026-10-15T00:00:00Z");
    private final StoreFalso almacen = new StoreFalso();
    private final UbicacionService servicio = new UbicacionService(despacho, regla, almacen, reloj);

    @BeforeEach
    void asignacion() {
        when(despacho.repartidorDe(7L)).thenReturn(Optional.of(3L));
    }

    @Test
    void soloLaUltimaPosicionGana() {
        servicio.reportar(3L, 4.1, -74.1);
        servicio.reportar(3L, 4.2, -74.2);

        assertThat(servicio.ultimaDePedido(7L))
                .contains(new UbicacionActual(3L, 4.2, -74.2, reloj.ahora()));
    }

    @Test
    void pedidoSinRepartidorDevuelveVacio() {
        when(despacho.repartidorDe(8L)).thenReturn(Optional.empty());

        assertThat(servicio.ultimaDePedido(8L)).isEmpty();
    }

    private static final class StoreFalso implements UbicacionStore {
        private final Map<Long, UbicacionActual> datos = new HashMap<>();

        @Override
        public void guardar(UbicacionActual actual) {
            datos.put(actual.repartidorId(), actual);
        }

        @Override
        public Optional<UbicacionActual> ultimaDe(Long repartidorId) {
            return Optional.ofNullable(datos.get(repartidorId));
        }
    }
}
