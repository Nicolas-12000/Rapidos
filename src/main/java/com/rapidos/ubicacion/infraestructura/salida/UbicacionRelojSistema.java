package com.rapidos.ubicacion.infraestructura.salida;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.rapidos.ubicacion.aplicacion.puertos.salida.Reloj;

@Component
class UbicacionRelojSistema implements Reloj {

    @Override
    public Instant ahora() {
        return Instant.now();
    }
}
