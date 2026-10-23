package com.rapidos.pedidos.infraestructura.salida.menu;

import java.time.Clock;
import java.time.Duration;

import com.rapidos.support.ClockAvanzable;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CacheConTtlTest {

    private final ClockAvanzable reloj = new ClockAvanzable();
    private final CacheConTtl<Long, String> cache = new CacheConTtl<>(Duration.ofMinutes(5), reloj);
    private final AtomicInteger cargas = new AtomicInteger();

    private String cargar(Long llave) {
        cargas.incrementAndGet();
        return "menu-" + llave;
    }

    @Test
    void segundaLecturaNoVuelveACargar() {
        cache.obtener(1L, this::cargar);
        cache.obtener(1L, this::cargar);

        assertThat(cargas).hasValue(1);
    }

    @Test
    void recargaCuandoVenceElTtl() {
        cache.obtener(1L, this::cargar);
        reloj.avanzar(Duration.ofMinutes(6));
        cache.obtener(1L, this::cargar);

        assertThat(cargas).hasValue(2);
    }

    @Test
    void invalidarObligaARecargar() {
        cache.obtener(1L, this::cargar);
        cache.invalidar(1L);
        cache.obtener(1L, this::cargar);

        assertThat(cargas).hasValue(2);
    }

    @Test
    void cadaRestauranteTieneSuPropiaEntrada() {
        assertThat(cache.obtener(1L, this::cargar)).isEqualTo("menu-1");
        assertThat(cache.obtener(2L, this::cargar)).isEqualTo("menu-2");

        assertThat(cargas).hasValue(2);
    }

    /** Simula el pico: 50 pedidos piden el mismo menú al mismo tiempo. */
    @Test
    void cincuentaHilosALaVezCarganUnaSolaVez() throws Exception {
        var cacheReal = new CacheConTtl<Long, String>(Duration.ofMinutes(5), Clock.systemUTC());
        var largada = new CountDownLatch(1);

        try (var hilos = Executors.newFixedThreadPool(50)) {
            var resultados = new ArrayList<Future<String>>();
            for (int i = 0; i < 50; i++) {
                resultados.add(hilos.submit(() -> {
                    largada.await(); // todos esperan la señal para arrancar juntos
                    return cacheReal.obtener(1L, llave -> {
                        cargas.incrementAndGet();
                        dormir(200); // simula una consulta lenta a PostgreSQL
                        return "menu";
                    });
                }));
            }
            largada.countDown(); // ¡arrancan los 50!
            for (var resultado : resultados) {
                assertThat(resultado.get()).isEqualTo("menu");
            }
        }

        assertThat(cargas).hasValue(1); // solo UNA consulta a la base de datos
    }

    private static void dormir(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}