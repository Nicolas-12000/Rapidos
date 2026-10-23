package com.rapidos.ubicacion.infraestructura.salida;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rapidos.ubicacion.dominio.UbicacionActual;
import com.sun.net.httpserver.HttpServer;

/** Firebase falso: un servidor HTTP local. No necesita un proyecto real. */
class UbicacionFirebaseTest {

    private static final Instant AHORA = Instant.parse("2026-01-01T10:00:00Z");

    private HttpServer servidor;
    private final AtomicReference<String> ultimoMetodo = new AtomicReference<>();
    private final AtomicReference<String> ultimaRuta = new AtomicReference<>();
    private final AtomicReference<String> ultimoCuerpo = new AtomicReference<>();
    private volatile int estado = 200;
    private volatile String respuesta = "null";

    @BeforeEach
    void arrancar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", ex -> {
            ultimoMetodo.set(ex.getRequestMethod());
            ultimaRuta.set(ex.getRequestURI().toString());
            ultimoCuerpo.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] cuerpo = respuesta.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(estado, cuerpo.length);
            ex.getResponseBody().write(cuerpo);
            ex.close();
        });
        servidor.start();
    }

    @AfterEach
    void parar() {
        servidor.stop(0);
    }

    private String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    private UbicacionFirebase almacen(String token, long ttl, Instant ahora) {
        Clock reloj = Clock.fixed(ahora, ZoneOffset.UTC);
        return new UbicacionFirebase(url(), token, ttl, reloj, Duration.ofSeconds(2));
    }

    @Test
    void guardaConPutEnElNodoDelRepartidor() {
        almacen("", 30, AHORA).guardar(new UbicacionActual(7L, 4.6, -74.1, AHORA));

        assertThat(ultimoMetodo.get()).isEqualTo("PUT");
        assertThat(ultimaRuta.get()).isEqualTo("/ubicaciones/7.json");
        assertThat(ultimoCuerpo.get()).contains("\"lat\":4.6").contains("\"lon\":-74.1")
                .contains("\"hora\":\"2026-01-01T10:00:00Z\"");
    }

    @Test
    void enviaElTokenCuandoLoHay() {
        almacen("secreto", 30, AHORA).guardar(new UbicacionActual(7L, 4.6, -74.1, AHORA));

        assertThat(ultimaRuta.get()).isEqualTo("/ubicaciones/7.json?auth=secreto");
    }

    @Test
    void leeLaUltimaPosicionVigente() {
        respuesta = "{\"hora\":\"2026-01-01T09:59:50Z\",\"lat\":4.6,\"lon\":-74.1}";

        assertThat(almacen("", 30, AHORA).ultimaDe(7L))
                .contains(new UbicacionActual(7L, 4.6, -74.1, Instant.parse("2026-01-01T09:59:50Z")));
    }

    @Test
    void descartaLaPosicionVencida() {
        respuesta = "{\"hora\":\"2026-01-01T09:58:00Z\",\"lat\":4.6,\"lon\":-74.1}";

        assertThat(almacen("", 30, AHORA).ultimaDe(7L)).isEmpty();
    }

    @Test
    void sinNodoDevuelveVacio() {
        respuesta = "null";

        assertThat(almacen("", 30, AHORA).ultimaDe(7L)).isEmpty();
    }

    @Test
    void siFirebaseFallaNoLanzaYDevuelveVacio() {
        estado = 500;
        UbicacionFirebase almacen = almacen("", 30, AHORA);

        almacen.guardar(new UbicacionActual(7L, 4.6, -74.1, AHORA));
        assertThat(almacen.ultimaDe(7L)).isEmpty();
    }

    @Test
    void siFirebaseNoResponderNoLanza() {
        String urlCaida = url();
        servidor.stop(0);
        UbicacionFirebase almacen = new UbicacionFirebase(urlCaida, "", 30,
                Clock.fixed(AHORA, ZoneOffset.UTC), Duration.ofMillis(500));

        almacen.guardar(new UbicacionActual(7L, 4.6, -74.1, AHORA));
        assertThat(almacen.ultimaDe(7L)).isEmpty();
    }

    @Test
    void sinUrlFallaAlCrearse() {
        assertThatThrownBy(() -> new UbicacionFirebase("", "", 30, Clock.systemUTC(), Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
