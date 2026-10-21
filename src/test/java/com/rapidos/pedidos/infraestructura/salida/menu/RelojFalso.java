package com.rapidos.pedidos.infraestructura.salida.menu;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Reloj que avanza solo cuando la prueba lo pide. */
class RelojFalso extends Clock {

    private Instant ahora = Instant.parse("2026-10-14T19:00:00Z"); // 7:00 p. m., el pico

    void avanzar(Duration tiempo) {
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