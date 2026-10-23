package com.rapidos.pedidos.infraestructura.salida.eventos;

import java.util.List;

import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.stereotype.Component;

import com.rapidos.pedidos.aplicacion.consultas.PublicacionPendiente;
import com.rapidos.pedidos.aplicacion.puertos.salida.PublicacionesPendientes;

@Component
class PublicacionesModulith implements PublicacionesPendientes {

    private final EventPublicationRegistry registro;

    PublicacionesModulith(EventPublicationRegistry registro) {
        this.registro = registro;
    }

    @Override
    public List<PublicacionPendiente> incompletas() {
        return registro.findIncompletePublications().stream()
                .map(pub -> new PublicacionPendiente(
                        pub.getIdentifier().toString(),
                        pub.getEvent().getClass().getSimpleName(),
                        pub.getTargetIdentifier().getValue(),
                        pub.getPublicationDate().toString()))
                .toList();
    }
}
