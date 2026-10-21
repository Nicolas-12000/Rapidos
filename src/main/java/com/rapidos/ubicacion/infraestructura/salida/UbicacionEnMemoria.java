package com.rapidos.ubicacion.infraestructura.salida;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.rapidos.ubicacion.aplicacion.puertos.salida.UbicacionStore;
import com.rapidos.ubicacion.dominio.UbicacionActual;

public class UbicacionEnMemoria implements UbicacionStore {

    private final long ttlSegundos;
    private final Clock reloj;
    private final ConcurrentMap<Long, UbicacionActual> ubicaciones = new ConcurrentHashMap<>();

    public UbicacionEnMemoria(long ttlSegundos, Clock reloj) {
        if (ttlSegundos < 0) {
            throw new IllegalArgumentException("El TTL de ubicación no puede ser negativo");
        }
        this.ttlSegundos = ttlSegundos;
        this.reloj = reloj;
    }

    @Override
    public void guardar(UbicacionActual actual) {
        ubicaciones.put(actual.repartidorId(), actual);
    }

    @Override
    public Optional<UbicacionActual> ultimaDe(Long repartidorId) {
        return Optional.ofNullable(ubicaciones.get(repartidorId)).filter(this::vigente);
    }

    private boolean vigente(UbicacionActual ubicacion) {
        Instant limite = ubicacion.hora().plusSeconds(ttlSegundos);
        return !limite.isBefore(reloj.instant());
    }
}
