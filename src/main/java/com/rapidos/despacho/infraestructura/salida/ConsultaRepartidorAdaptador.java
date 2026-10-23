package com.rapidos.despacho.infraestructura.salida;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rapidos.despacho.ConsultaRepartidor;
import com.rapidos.despacho.aplicacion.puertos.entrada.ConsultarRepartidorUseCase;

/** Publica hacia afuera solo el puerto de entrada. El servicio no es la API del módulo. */
@Component
class ConsultaRepartidorAdaptador implements ConsultaRepartidor {

    private final ConsultarRepartidorUseCase consulta;

    ConsultaRepartidorAdaptador(ConsultarRepartidorUseCase consulta) {
        this.consulta = consulta;
    }

    @Override
    public Optional<Long> repartidorDe(Long pedidoId) {
        return consulta.repartidorDe(pedidoId);
    }
}
