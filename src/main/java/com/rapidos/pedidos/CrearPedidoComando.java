package com.rapidos.pedidos;

import java.util.List;

public record CrearPedidoComando(Long restauranteId, Long clienteId, List<LineaPedido> items) {
}
