package com.rapidos.ubicacion;

import java.util.Optional;

/** API pública del módulo ubicacion. Dueño: Dev 4. Solo memoria, sin BD de pedidos (ADR-002). */
public interface UbicacionApi {

    /** Guarda solo la última posición del repartidor (el último valor gana). */
    void reportar(Long repartidorId, double lat, double lon);

    Optional<UbicacionActual> ultimaDePedido(Long pedidoId);

    /** 5, 10 o 15 segundos según la regla de degradación. */
    int frecuenciaSugeridaSegundos();
}
