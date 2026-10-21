package com.rapidos.despacho.infraestructura.entrada;

import com.rapidos.despacho.aplicacion.puertos.entrada.ProcesarPedidoConfirmado;
import com.rapidos.pedidos.PedidoConfirmado;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class PedidoConfirmadoListener {

    private final ProcesarPedidoConfirmado despacho;

    PedidoConfirmadoListener(ProcesarPedidoConfirmado despacho) {
        this.despacho = despacho;
    }

    @ApplicationModuleListener
    void alConfirmar(PedidoConfirmado pedido) {
        despacho.procesar(pedido);
    }
}
