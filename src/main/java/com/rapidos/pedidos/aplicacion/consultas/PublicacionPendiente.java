package com.rapidos.pedidos.aplicacion.consultas;

/** Una publicación de evento aún no completada (lectura operativa). */
public record PublicacionPendiente(String id, String evento, String listener, String publicadoEn) {
}
