package com.rapidos.ubicacion.infraestructura.salida;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rapidos.ubicacion.aplicacion.puertos.salida.UbicacionStore;
import com.rapidos.ubicacion.dominio.UbicacionActual;

/**
 * Última posición en Firebase Realtime Database, por su API REST.
 * Guarda en {@code /ubicaciones/{repartidorId}.json}. El vencimiento (TTL) lo aplica
 * este adaptador al leer. Si Firebase falla o no responde, no lanza: devuelve
 * "sin ubicación" para no tumbar el pedido (ADR-002).
 */
public class UbicacionFirebase implements UbicacionStore {

    private static final Logger LOG = Logger.getLogger(UbicacionFirebase.class.getName());
    private static final Pattern NUMERO = Pattern.compile("\"%s\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?)");
    private static final Pattern HORA = Pattern.compile("\"hora\"\\s*:\\s*\"([^\"]+)\"");

    private final String baseUrl;
    private final String authToken;
    private final long ttlSegundos;
    private final Clock reloj;
    private final Duration timeout;
    private final HttpClient cliente;

    public UbicacionFirebase(String baseUrl, String authToken, long ttlSegundos, Clock reloj, Duration timeout) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Falta la URL de Firebase (rapidos.ubicacion.firebase.url)");
        }
        if (ttlSegundos < 0) {
            throw new IllegalArgumentException("El TTL de ubicación no puede ser negativo");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.authToken = authToken == null ? "" : authToken.trim();
        this.ttlSegundos = ttlSegundos;
        this.reloj = reloj;
        this.timeout = timeout;
        this.cliente = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    @Override
    public void guardar(UbicacionActual actual) {
        String json = String.format(Locale.ROOT, "{\"lat\":%s,\"lon\":%s,\"hora\":\"%s\"}",
                actual.lat(), actual.lon(), actual.hora());
        HttpRequest peticion = HttpRequest.newBuilder(uriDe(actual.repartidorId()))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() / 100 != 2) {
                LOG.warning("Firebase respondió " + respuesta.statusCode() + " al guardar la ubicación");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo guardar la ubicación en Firebase: {0}", String.valueOf(e));
        }
    }

    @Override
    public Optional<UbicacionActual> ultimaDe(Long repartidorId) {
        HttpRequest peticion = HttpRequest.newBuilder(uriDe(repartidorId)).timeout(timeout).GET().build();
        try {
            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() / 100 != 2) {
                LOG.warning("Firebase respondió " + respuesta.statusCode() + " al leer la ubicación");
                return Optional.empty();
            }
            return leer(repartidorId, respuesta.body()).filter(this::vigente);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo leer la ubicación de Firebase: {0}", String.valueOf(e));
            return Optional.empty();
        }
    }

    private URI uriDe(Long repartidorId) {
        String auth = authToken.isEmpty() ? "" : "?auth=" + URLEncoder.encode(authToken, StandardCharsets.UTF_8);
        return URI.create(baseUrl + "/ubicaciones/" + repartidorId + ".json" + auth);
    }

    private Optional<UbicacionActual> leer(Long repartidorId, String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank() || cuerpo.trim().equals("null")) {
            return Optional.empty();
        }
        Double lat = numero(cuerpo, "lat");
        Double lon = numero(cuerpo, "lon");
        Matcher hora = HORA.matcher(cuerpo);
        if (lat == null || lon == null || !hora.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new UbicacionActual(repartidorId, lat, lon, Instant.parse(hora.group(1))));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static Double numero(String json, String campo) {
        Matcher m = Pattern.compile(String.format(NUMERO.pattern(), campo)).matcher(json);
        return m.find() ? Double.valueOf(m.group(1)) : null;
    }

    private boolean vigente(UbicacionActual ubicacion) {
        return !ubicacion.hora().plusSeconds(ttlSegundos).isBefore(reloj.instant());
    }
}
