package com.rapidos.despacho;

import java.util.Optional;

/** API que ubicación puede ver. El resto del módulo queda adentro. */
public interface ConsultaRepartidor {

    Optional<Long> repartidorDe(Long pedidoId);
}
