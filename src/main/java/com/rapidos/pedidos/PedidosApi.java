package com.rapidos.pedidos;

import java.util.Optional;

/** API pública del módulo pedidos (orquestador). Dueños: Dev 1 + Dev 5. */
public interface PedidosApi {

    PedidoResumen crear(CrearPedidoComando comando, String llavePedido);

    Optional<PedidoResumen> consultar(Long pedidoId);
}
