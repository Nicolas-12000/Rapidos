package com.rapidos.despacho.aplicacion.puertos.salida;

import com.rapidos.pedidos.PedidoConfirmado;

public interface Avisador {

    void avisarRestaurante(PedidoConfirmado pedido);

    void avisarRepartidor(Long repartidorId, PedidoConfirmado pedido);
}
