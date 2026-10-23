package com.rapidos.pedidos.aplicacion.puertos.salida;

import java.util.List;

import com.rapidos.pedidos.aplicacion.consultas.PublicacionPendiente;

/** Lectura de la cola de eventos de módulo (infra Modulith/JDBC). */
public interface PublicacionesPendientes {

    List<PublicacionPendiente> incompletas();
}
