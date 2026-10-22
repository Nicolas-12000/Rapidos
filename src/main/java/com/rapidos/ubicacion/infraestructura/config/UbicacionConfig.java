package com.rapidos.ubicacion.infraestructura.config;

import java.time.Clock;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rapidos.ubicacion.aplicacion.puertos.entrada.ReportarUbicacionUseCase;
import com.rapidos.ubicacion.aplicacion.puertos.salida.RepartidorDePedido;
import com.rapidos.ubicacion.aplicacion.puertos.salida.UbicacionStore;
import com.rapidos.ubicacion.aplicacion.servicios.UbicacionService;
import com.rapidos.ubicacion.dominio.ReglaDegradacionGps;
import com.rapidos.ubicacion.infraestructura.salida.UbicacionEnMemoria;
import com.rapidos.ubicacion.infraestructura.salida.UbicacionFirebase;

@Configuration
class UbicacionConfig {

    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    ReglaDegradacionGps reglaDegradacionGps(
            @Value("${rapidos.ubicacion.ventana-medidas:20}") int tamanoVentana,
            @Value("${rapidos.ubicacion.minimo-mediciones:5}") int minimoMediciones) {
        return new ReglaDegradacionGps(tamanoVentana, minimoMediciones);
    }

    @Bean
    UbicacionStore ubicacionStore(
            @Value("${rapidos.ubicacion.store:memoria}") String tipo,
            @Value("${rapidos.ubicacion.ttl-segundos:30}") long ttlSegundos,
            @Value("${rapidos.ubicacion.firebase.url:}") String firebaseUrl,
            @Value("${rapidos.ubicacion.firebase.auth-token:}") String firebaseToken,
            @Value("${rapidos.ubicacion.firebase.timeout-ms:2000}") long timeoutMs,
            Clock reloj) {
        if ("firebase".equalsIgnoreCase(tipo)) {
            return new UbicacionFirebase(firebaseUrl, firebaseToken, ttlSegundos, reloj,
                    Duration.ofMillis(timeoutMs));
        }
        return new UbicacionEnMemoria(ttlSegundos, reloj);
    }

    @Bean
    ReportarUbicacionUseCase reportarUbicacion(
            RepartidorDePedido repartidores,
            ReglaDegradacionGps regla,
            UbicacionStore almacen,
            Clock reloj) {
        return new UbicacionService(repartidores, regla, almacen, reloj);
    }
}
