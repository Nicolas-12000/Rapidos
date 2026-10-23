package com.rapidos.pedidos.aplicacion.consultas;

import java.util.List;

/** Resumen de la cola de eventos para operaciones. */
public record ColaEventos(int pendientes, List<PublicacionPendiente> publicaciones) {
}
