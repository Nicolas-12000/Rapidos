package com.rapidos.pedidos;

import java.util.Optional;

/** API pública del módulo pedidos (orquestador). Dueños: Dev 1 + Aprendiz. */
public interface PedidosApi {

    /** Crea el pedido. Misma llave de idempotencia = mismo pedido (ADR-001). */
    PedidoResumen crear(CrearPedidoComando comando, String llaveIdempotencia);

    Optional<PedidoResumen> consultar(Long pedidoId);
}
