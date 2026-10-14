package com.rapidos.pedidos.internal;

import com.rapidos.pedidos.CrearPedidoComando;
import com.rapidos.pedidos.LineaPedido;
import com.rapidos.pedidos.PedidoResumen;
import com.rapidos.pedidos.PedidosApi;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
class PedidosController {

    private final PedidosApi pedidos;

    PedidosController(PedidosApi pedidos) {
        this.pedidos = pedidos;
    }

    @PostMapping
    ResponseEntity<PedidoResumen> crear(
            @RequestHeader(value = "Idempotency-Key", required = false) String llavePedido,
            @Valid @RequestBody CrearPedidoRequest body) {
        if (llavePedido == null || llavePedido.isBlank()) {
            throw new IllegalArgumentException("Falta la cabecera Idempotency-Key");
        }
        var comando = new CrearPedidoComando(
                body.restauranteId(),
                body.clienteId(),
                body.items().stream()
                        .map(linea -> new LineaPedido(linea.itemId(), linea.cantidad()))
                        .toList());
        var resumen = pedidos.crear(comando, llavePedido);
        return ResponseEntity.status(201).body(resumen);
    }

    @GetMapping("/{pedidoId}")
    ResponseEntity<PedidoResumen> consultar(@PathVariable Long pedidoId) {
        return pedidos.consultar(pedidoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
