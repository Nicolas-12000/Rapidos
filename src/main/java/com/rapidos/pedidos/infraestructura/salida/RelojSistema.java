package com.rapidos.pedidos.infraestructura.salida;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.rapidos.pedidos.aplicacion.puertos.salida.Reloj;

@Component
class RelojSistema implements Reloj {

    @Override
    public Instant ahora() {
        return Instant.now();
    }

    @Override
    public long nanos() {
        return System.nanoTime();
    }
}
