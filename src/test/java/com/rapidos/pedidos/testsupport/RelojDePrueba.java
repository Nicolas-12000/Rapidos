package com.rapidos.pedidos.testsupport;

import java.time.Instant;

import com.rapidos.pedidos.aplicacion.puertos.salida.Reloj;

/** Implementación fija del puerto {@link Reloj} para pruebas de pedidos. */
public final class RelojDePrueba implements Reloj {

    public static final Instant INSTANTE_FIJO = Instant.parse("2026-01-01T00:00:00Z");

    private final Instant ahora;
    private final long nanos;

    public RelojDePrueba() {
        this(INSTANTE_FIJO, 0L);
    }

    public RelojDePrueba(Instant ahora, long nanos) {
        this.ahora = ahora;
        this.nanos = nanos;
    }

    @Override
    public Instant ahora() {
        return ahora;
    }

    @Override
    public long nanos() {
        return nanos;
    }
}
