package com.rapidos.pedidos.infraestructura.entrada;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rapidos.pedidos.aplicacion.consultas.ColaEventos;
import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarColaUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarMenuUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarPedidoUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoComando;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoUseCase;
import com.rapidos.pedidos.dominio.pedido.LineaPedido;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
class PedidosController {

    private final CrearPedidoUseCase crearPedido;
    private final ConsultarPedidoUseCase consultarPedido;
    private final ConsultarMenuUseCase menu;
    private final ConsultarColaUseCase cola;

    PedidosController(
            CrearPedidoUseCase crearPedido,
            ConsultarPedidoUseCase consultarPedido,
            ConsultarMenuUseCase menu,
            ConsultarColaUseCase cola) {
        this.crearPedido = crearPedido;
        this.consultarPedido = consultarPedido;
        this.menu = menu;
        this.cola = cola;
    }

    @PostMapping("/pedidos")
    ResponseEntity<PedidoResumenResponse> crear(
            @RequestHeader(value = "Idempotency-Key", required = false) String llavePedido,
            @Valid @RequestBody CrearPedidoRequest body) {
        var comando = new CrearPedidoComando(
                body.restauranteId(),
                body.clienteId(),
                body.items().stream()
                        .map(linea -> new LineaPedido(linea.itemId(), linea.cantidad()))
                        .toList());
        return ResponseEntity.status(201).body(PedidoResumenResponse.de(crearPedido.crear(comando, llavePedido)));
    }

    @GetMapping("/pedidos/{pedidoId}")
    ResponseEntity<PedidoResumenResponse> consultar(@PathVariable Long pedidoId) {
        return consultarPedido.consultar(pedidoId)
                .map(PedidoResumenResponse::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cola")
    ColaEventos pendientes() {
        return cola.resumen();
    }

    @GetMapping("/restaurantes/{restauranteId}/menu")
    ResponseEntity<List<ItemMenuResponse>> menu(@PathVariable Long restauranteId) {
        return menu.menuSiActivo(restauranteId)
                .map(platos -> platos.stream().map(ItemMenuResponse::de).toList())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
