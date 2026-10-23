package com.rapidos.pedidos.aplicacion.servicios;

import com.rapidos.pedidos.aplicacion.consultas.ColaEventos;
import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarColaUseCase;
import com.rapidos.pedidos.aplicacion.puertos.salida.PublicacionesPendientes;

public class ConsultarColaService implements ConsultarColaUseCase {

    private final PublicacionesPendientes publicaciones;

    public ConsultarColaService(PublicacionesPendientes publicaciones) {
        this.publicaciones = publicaciones;
    }

    @Override
    public ColaEventos resumen() {
        var detalle = publicaciones.incompletas();
        return new ColaEventos(detalle.size(), detalle);
    }
}
