package com.rapidos.ubicacion.infraestructura.salida;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rapidos.despacho.ConsultaRepartidor;
import com.rapidos.ubicacion.aplicacion.puertos.salida.RepartidorDePedido;

@Component
public class RepartidorDespachoAdapter implements RepartidorDePedido {

    private final ConsultaRepartidor consulta;

    public RepartidorDespachoAdapter(ConsultaRepartidor consulta) {
        this.consulta = consulta;
    }

    @Override
    public Optional<Long> repartidorDe(Long pedidoId) {
        return consulta.repartidorDe(pedidoId);
    }
}
