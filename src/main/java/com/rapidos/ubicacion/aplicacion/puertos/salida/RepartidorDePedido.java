package com.rapidos.ubicacion.aplicacion.puertos.salida;

import java.util.Optional;

/** Puerto de salida hacia despacho. Ubicación no llama a su controlador. */
public interface RepartidorDePedido {

    Optional<Long> repartidorDe(Long pedidoId);
}
