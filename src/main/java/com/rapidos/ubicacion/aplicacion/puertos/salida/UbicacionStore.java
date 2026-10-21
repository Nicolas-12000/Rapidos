package com.rapidos.ubicacion.aplicacion.puertos.salida;

import java.util.Optional;

import com.rapidos.ubicacion.dominio.UbicacionActual;

/** Última posición en memoria. El vencimiento lo aplica el adaptador. */
public interface UbicacionStore {

    void guardar(UbicacionActual actual);

    Optional<UbicacionActual> ultimaDe(Long repartidorId);
}
