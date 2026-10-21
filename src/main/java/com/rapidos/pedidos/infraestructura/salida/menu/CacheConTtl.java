package com.rapidos.pedidos.infraestructura.salida.menu;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Caché en memoria con vencimiento (TTL). ADR-001, complemento C.
 * En producción se reemplazaría por una caché gestionada (p. ej. ElastiCache).
 *
 * @param <K> tipo de la llave (ej. id del restaurante)
 * @param <V> tipo del valor guardado (ej. el menú)
 */
final class CacheConTtl<K, V> {

    /** Un valor guardado y el momento en que deja de servir. */
    private record Entrada<T>(T valor, Instant vence) {}

    private final Map<K, Entrada<V>> entradas = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Clock clock;

    CacheConTtl(Duration ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    /**
     * Devuelve el valor guardado si sigue vigente; si no, lo carga UNA sola vez
     * aunque muchos hilos lo pidan al mismo tiempo.
     */
    V obtener(K llave, Function<K, V> cargar) {
        // Camino rápido: lectura sin bloqueo (casi todas las llamadas en el pico)
        Entrada<V> actual = entradas.get(llave);
        if (vigente(actual)) {
            return actual.valor();
        }
        // Camino lento: compute bloquea esa llave; solo un hilo consulta la base de datos
        return entradas.compute(llave, (k, previa) ->
                vigente(previa)
                        ? previa
                        : new Entrada<>(cargar.apply(k), clock.instant().plus(ttl))
        ).valor();
    }

    void invalidar(K llave) {
        entradas.remove(llave);
    }

    void invalidarTodo() {
        entradas.clear();
    }

    private boolean vigente(Entrada<V> entrada) {
        return entrada != null && clock.instant().isBefore(entrada.vence());
    }
}