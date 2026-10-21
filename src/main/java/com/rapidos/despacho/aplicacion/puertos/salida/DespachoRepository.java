package com.rapidos.despacho.aplicacion.puertos.salida;

import java.time.Instant;
import java.util.Optional;

import com.rapidos.despacho.dominio.EstadoEntrega;

public interface DespachoRepository {

    boolean crearAsignacion(Long pedidoId, Long repartidorId, Instant asignadoEn);

    boolean guardarEstado(
            Long pedidoId,
            Long repartidorId,
            EstadoEntrega estado,
            String llave,
            Instant registradoEn);

    void actualizarEstado(Long pedidoId, EstadoEntrega estado, Instant actualizadoEn);

    Optional<Long> repartidorDe(Long pedidoId);

    Optional<EstadoEntrega> estadoDe(Long pedidoId);
}
