package com.rapidos.pedidos.aplicacion.puertos.entrada;

import java.util.List;

import com.rapidos.pedidos.dominio.pedido.LineaPedido;

public record CrearPedidoComando(Long restauranteId, Long clienteId, List<LineaPedido> items) {
}
