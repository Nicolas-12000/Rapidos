package com.rapidos.despacho.internal;

import com.rapidos.pedidos.PedidoConfirmado;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class PedidoConfirmadoListener {

    private final DespachoService despacho;

    PedidoConfirmadoListener(DespachoService despacho) {
        this.despacho = despacho;
    }

    @ApplicationModuleListener
    void alConfirmar(PedidoConfirmado pedido) {
        despacho.procesarConfirmacion(pedido);
    }
}
