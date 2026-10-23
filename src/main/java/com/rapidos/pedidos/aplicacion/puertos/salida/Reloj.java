package com.rapidos.pedidos.aplicacion.puertos.salida;

import java.time.Instant;

/** Puerto de tiempo. El servicio no llama a Instant.now(). */
public interface Reloj {

    Instant ahora();

    long nanos();
}
