package com.rapidos.ubicacion.aplicacion.puertos.entrada;

import java.util.Optional;

import com.rapidos.ubicacion.dominio.UbicacionActual;

public interface ReportarUbicacionUseCase {

    void reportar(Long repartidorId, double lat, double lon);

    Optional<UbicacionActual> ultimaDePedido(Long pedidoId);

    int frecuenciaSugeridaSegundos();
}
