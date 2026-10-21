package com.rapidos.ubicacion.infraestructura.entrada;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rapidos.ubicacion.aplicacion.puertos.entrada.ReportarUbicacionUseCase;
import com.rapidos.ubicacion.dominio.UbicacionActual;

@RestController
@RequestMapping("/api")
class UbicacionController {

    private final ReportarUbicacionUseCase ubicacion;

    UbicacionController(ReportarUbicacionUseCase ubicacion) {
        this.ubicacion = ubicacion;
    }

    @PutMapping("/repartidores/{repartidorId}/ubicacion")
    ResponseEntity<Void> reportar(
            @PathVariable Long repartidorId,
            @RequestBody(required = false) ReportarUbicacionRequest request) {
        if (request == null || request.lat() == null || request.lon() == null
                || request.lat() < -90 || request.lat() > 90
                || request.lon() < -180 || request.lon() > 180) {
            return ResponseEntity.badRequest().build();
        }
        ubicacion.reportar(repartidorId, request.lat(), request.lon());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pedidos/{pedidoId}/ubicacion")
    ResponseEntity<UbicacionResponse> consultar(@PathVariable Long pedidoId) {
        return ubicacion.ultimaDePedido(pedidoId)
                .map(this::respuesta)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private UbicacionResponse respuesta(UbicacionActual actual) {
        return new UbicacionResponse(
                actual.lat(),
                actual.lon(),
                actual.hora(),
                ubicacion.frecuenciaSugeridaSegundos());
    }

    record ReportarUbicacionRequest(Double lat, Double lon) {
    }

    record UbicacionResponse(
            double lat,
            double lon,
            Instant hora,
            int frecuenciaSugeridaSegundos) {
    }
}
