package com.rapidos.despacho.infraestructura.salida;

import com.rapidos.despacho.aplicacion.puertos.salida.Avisador;
import com.rapidos.pedidos.PedidoConfirmado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class AvisadorDespacho implements Avisador {

    private static final Logger log = LoggerFactory.getLogger(AvisadorDespacho.class);

    @Override
    public void avisarRestaurante(PedidoConfirmado pedido) {
        log.info("Restaurante {} avisado del pedido {}", pedido.restauranteId(), pedido.pedidoId());
    }

    @Override
    public void avisarRepartidor(Long repartidorId, PedidoConfirmado pedido) {
        log.info("Repartidor {} avisado del pedido {}", repartidorId, pedido.pedidoId());
    }
}
