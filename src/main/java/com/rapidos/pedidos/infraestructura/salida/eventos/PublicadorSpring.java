package com.rapidos.pedidos.infraestructura.salida.eventos;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.rapidos.pedidos.aplicacion.puertos.salida.PublicadorDeEventos;

@Component
class PublicadorSpring implements PublicadorDeEventos {

    private final ApplicationEventPublisher eventos;

    public PublicadorSpring(ApplicationEventPublisher eventos) {
        this.eventos = eventos;
    }

    @Override
    public void publicar(Object evento) {
        eventos.publishEvent(evento);
    }
}
