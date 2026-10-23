package com.rapidos.despacho.aplicacion.puertos.entrada;

import java.util.Optional;

public interface ConsultarRepartidorUseCase {

    Optional<Long> repartidorDe(Long pedidoId);
}
