package com.rapidos.ubicacion.infraestructura.config;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rapidos.ubicacion.aplicacion.puertos.salida.UbicacionStore;
import com.rapidos.ubicacion.infraestructura.salida.UbicacionEnMemoria;
import com.rapidos.ubicacion.infraestructura.salida.UbicacionFirebase;

/**
 * Un {@link UbicacionStore}. El servicio no elige la tecnología:
 * {@code RAPIDOS_PERSISTENCIA_UBICACION=memoria|firebase}.
 */
@Configuration
class AdaptadorUbicacionStoreConfig {

    @Bean
    UbicacionStore ubicacionStore(
            @Value("${rapidos.persistencia.ubicacion:memoria}") String tipo,
            @Value("${rapidos.ubicacion.ttl-segundos:30}") long ttlSegundos,
            @Value("${rapidos.ubicacion.firebase.url:}") String url,
            @Value("${rapidos.ubicacion.firebase.auth-token:}") String authToken,
            @Value("${rapidos.ubicacion.firebase.timeout-ms:2000}") long timeoutMs) {
        if ("firebase".equalsIgnoreCase(tipo)) {
            if (url == null || url.isBlank()) {
                throw new IllegalStateException(
                        "rapidos.persistencia.ubicacion=firebase requiere FIREBASE_DB_URL");
            }
            return new UbicacionFirebase(url, authToken, ttlSegundos, Clock.systemUTC(), Duration.ofMillis(timeoutMs));
        }
        return new UbicacionEnMemoria(ttlSegundos, Clock.systemUTC());
    }
}
