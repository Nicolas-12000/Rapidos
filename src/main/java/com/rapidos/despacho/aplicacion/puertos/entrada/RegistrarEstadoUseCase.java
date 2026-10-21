package com.rapidos.despacho.aplicacion.puertos.entrada;

import java.util.Optional;

import com.rapidos.despacho.dominio.EstadoEntrega;

public interface RegistrarEstadoUseCase {

    void registrarEstado(Long repartidorId, Long pedidoId, EstadoEntrega estado, String llave);

    Optional<EstadoEntrega> estadoDe(Long pedidoId);
}
