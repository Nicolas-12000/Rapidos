package com.rapidos.despacho.dominio;

/**
 * Valor de la llave de idempotencia (ADR-003).
 * La misma llave no se aplica dos veces: esta clase encapsula
 * la regla de dominio antes de que llegue al repositorio.
 */
public record LlaveEstado(String valor) {

    /** Valida al construir: nula o vacía nunca es válida. */
    public LlaveEstado {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("La llave de estado no puede ser nula ni vacía");
        }
        valor = valor.trim();
    }

    /** Punto único de creación para usar desde el servicio. */
    public static LlaveEstado de(String valor) {
        return new LlaveEstado(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
