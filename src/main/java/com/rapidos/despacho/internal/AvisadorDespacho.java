package com.rapidos.despacho.internal;

import com.rapidos.pedidos.PedidoConfirmado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class AvisadorDespacho {

    private static final Logger log = LoggerFactory.getLogger(AvisadorDespacho.class);

    void avisarRestaurante(PedidoConfirmado pedido) {
        log.info("Restaurante {} avisado del pedido {}", pedido.restauranteId(), pedido.pedidoId());
    }

    void avisarRepartidor(Long repartidorId, PedidoConfirmado pedido) {
        log.info("Repartidor {} avisado del pedido {}", repartidorId, pedido.pedidoId());
    }
}
