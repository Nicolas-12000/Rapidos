package com.rapidos.pedidos.aplicacion.puertos.entrada;

import java.util.Optional;

import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

public interface ConsultarPedidoUseCase {

    Optional<PedidoResumen> consultar(Long pedidoId);
}
