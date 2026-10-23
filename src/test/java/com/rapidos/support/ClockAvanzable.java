package com.rapidos.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Reloj de prueba que avanza solo cuando la prueba lo pide. */
public final class ClockAvanzable extends Clock {

    private Instant ahora;

    public ClockAvanzable(Instant inicio) {
        this.ahora = inicio;
    }

    public ClockAvanzable() {
        this(Instant.parse("2026-10-14T19:00:00Z"));
    }

    public void avanzar(Duration tiempo) {
        ahora = ahora.plus(tiempo);
    }

    @Override
    public Instant instant() {
        return ahora;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }
}
