package com.rapidos.despacho.infraestructura.entrada;

import com.rapidos.despacho.aplicacion.puertos.entrada.RegistrarEstadoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repartidores")
class DespachoController {

    private final RegistrarEstadoUseCase despacho;

    DespachoController(RegistrarEstadoUseCase despacho) {
        this.despacho = despacho;
    }

    @PostMapping("/{repartidorId}/estado")
    ResponseEntity<Void> registrarEstado(
            @PathVariable Long repartidorId,
            @Valid @RequestBody RegistrarEstadoRequest request) {
        despacho.registrarEstado(repartidorId, request.pedidoId(), request.estado(), request.llave());
        return ResponseEntity.noContent().build();
    }
}
