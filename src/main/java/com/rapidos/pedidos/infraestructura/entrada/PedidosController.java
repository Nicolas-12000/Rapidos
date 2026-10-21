package com.rapidos.pedidos.infraestructura.entrada;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.modulith.events.core.TargetEventPublication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarMenuUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarPedidoUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoComando;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoUseCase;
import com.rapidos.pedidos.dominio.menu.ItemMenu;
import com.rapidos.pedidos.dominio.pedido.LineaPedido;
import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

import jakarta.validation.Valid;

@RestController
class PedidosController {

    private final CrearPedidoUseCase crearPedido;
    private final ConsultarPedidoUseCase consultarPedido;
    private final ConsultarMenuUseCase menu;
    private final EventPublicationRegistry publicaciones;

    PedidosController(
            CrearPedidoUseCase crearPedido,
            ConsultarPedidoUseCase consultarPedido,
            ConsultarMenuUseCase menu,
            EventPublicationRegistry publicaciones) {
        this.crearPedido = crearPedido;
        this.consultarPedido = consultarPedido;
        this.menu = menu;
        this.publicaciones = publicaciones;
    }

    @PostMapping("/api/pedidos")
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
        return ResponseEntity.status(201).body(crearPedido.crear(comando, llavePedido));
    }

    @GetMapping("/api/pedidos/{pedidoId}")
    ResponseEntity<PedidoResumen> consultar(@PathVariable Long pedidoId) {
        return consultarPedido.consultar(pedidoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/cola")
    Map<String, Object> pendientes() {
        List<Map<String, Object>> detalle = publicaciones.findIncompletePublications().stream()
                .map(this::resumen)
                .toList();
        return Map.of(
                "pendientes", detalle.size(),
                "publicaciones", detalle);
    }

    @GetMapping("/api/restaurantes/{restauranteId}/menu")
    ResponseEntity<List<ItemMenu>> menu(@PathVariable Long restauranteId) {
        if (!menu.restauranteActivo(restauranteId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(menu.menu(restauranteId));
    }

    private Map<String, Object> resumen(TargetEventPublication pub) {
        return Map.of(
                "id", pub.getIdentifier().toString(),
                "evento", pub.getEvent().getClass().getSimpleName(),
                "listener", pub.getTargetIdentifier().getValue(),
                "publicadoEn", pub.getPublicationDate().toString());
    }
}
