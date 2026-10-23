package com.rapidos.despacho.infraestructura.salida;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.rapidos.despacho.aplicacion.puertos.salida.Reloj;

@Component
class DespachoRelojSistema implements Reloj {

    @Override
    public Instant ahora() {
        return Instant.now();
    }
}
