package com.rapidos.pedidos.aplicacion.puertos.entrada;

import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

public interface CrearPedidoUseCase {

    PedidoResumen crear(CrearPedidoComando comando, String llavePedido);
}
