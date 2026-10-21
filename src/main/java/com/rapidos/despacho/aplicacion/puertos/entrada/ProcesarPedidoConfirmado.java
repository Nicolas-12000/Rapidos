package com.rapidos.despacho.aplicacion.puertos.entrada;

import com.rapidos.pedidos.PedidoConfirmado;

/** Lo que el listener llama cuando llega PedidoConfirmado. */
public interface ProcesarPedidoConfirmado {

    void procesar(PedidoConfirmado pedido);
}
