package com.rapidos.pedidos.internal;

import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.modulith.events.core.TargetEventPublication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cola")
class ColaController {

    private final EventPublicationRegistry publicaciones;

    ColaController(EventPublicationRegistry publicaciones) {
        this.publicaciones = publicaciones;
    }

    @GetMapping
    Map<String, Object> pendientes() {
        List<Map<String, Object>> detalle = publicaciones.findIncompletePublications().stream()
                .map(this::resumen)
                .toList();
        return Map.of(
                "pendientes", detalle.size(),
                "publicaciones", detalle);
    }

    private Map<String, Object> resumen(TargetEventPublication pub) {
        return Map.of(
                "id", pub.getIdentifier().toString(),
                "evento", pub.getEvent().getClass().getSimpleName(),
                "listener", pub.getTargetIdentifier().getValue(),
                "publicadoEn", pub.getPublicationDate().toString());
    }
}
