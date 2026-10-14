package com.rapidos.despacho;

import java.util.Optional;

/** API pública del módulo despacho. Dueño: Dev 3. */
public interface DespachoApi {

    /** Registra un cambio de estado. La misma llave no se procesa dos veces (ADR-003). */
    void registrarEstado(Long repartidorId, Long pedidoId, EstadoEntrega estado, String llave);

    Optional<Long> repartidorDe(Long pedidoId);

    Optional<EstadoEntrega> estadoDe(Long pedidoId);
}
