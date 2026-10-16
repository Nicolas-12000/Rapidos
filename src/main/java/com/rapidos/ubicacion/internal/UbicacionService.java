package com.rapidos.ubicacion.internal;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rapidos.despacho.DespachoApi;
import com.rapidos.ubicacion.UbicacionActual;
import com.rapidos.ubicacion.UbicacionApi;

@Service
class UbicacionService implements UbicacionApi {

    private final DespachoApi despacho;
    private final ReglaDegradacionGps reglaDegradacion;
    private final long ttlSegundos;
    private final Clock reloj;
    private final ConcurrentMap<Long, UbicacionActual> ubicaciones = new ConcurrentHashMap<>();

    @Autowired
    UbicacionService(
            DespachoApi despacho,
            ReglaDegradacionGps reglaDegradacion,
            @Value("${rapidos.ubicacion.ttl-segundos:30}") long ttlSegundos) {
        this(despacho, reglaDegradacion, ttlSegundos, Clock.systemUTC());
    }

    UbicacionService(
            DespachoApi despacho,
            ReglaDegradacionGps reglaDegradacion,
            long ttlSegundos,
            Clock reloj) {
        if (ttlSegundos < 0) {
            throw new IllegalArgumentException("El TTL de ubicación no puede ser negativo");
        }
        this.despacho = despacho;
        this.reglaDegradacion = reglaDegradacion;
        this.ttlSegundos = ttlSegundos;
        this.reloj = reloj;
    }

    @Override
    public void reportar(Long repartidorId, double lat, double lon) {
        ubicaciones.put(
                repartidorId,
                new UbicacionActual(repartidorId, lat, lon, reloj.instant()));
    }

    @Override
    public Optional<UbicacionActual> ultimaDePedido(Long pedidoId) {
        return despacho.repartidorDe(pedidoId)
                .map(ubicaciones::get)
                .filter(this::vigente);
    }

    @Override
    public int frecuenciaSugeridaSegundos() {
        return reglaDegradacion.frecuenciaSugeridaSegundos();
    }

    private boolean vigente(UbicacionActual ubicacion) {
        Instant limite = ubicacion.hora().plusSeconds(ttlSegundos);
        return !limite.isBefore(reloj.instant());
    }
}
