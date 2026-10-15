package com.rapidos.despacho.internal;

import com.rapidos.despacho.EstadoEntrega;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Repository
class DespachoRepository {

    private final JdbcClient jdbc;

    DespachoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    boolean crearAsignacion(Long pedidoId, Long repartidorId, Instant asignadoEn) {
        return jdbc.sql("""
                        INSERT INTO despacho_pedido
                            (pedido_id, repartidor_id, estado, asignado_en, actualizado_en)
                        VALUES
                            (:pedidoId, :repartidorId, :estado, :asignadoEn, :asignadoEn)
                        ON CONFLICT (pedido_id) DO NOTHING
                        """)
                .param("pedidoId", pedidoId)
                .param("repartidorId", repartidorId)
                .param("estado", EstadoEntrega.ASIGNADO.name())
                .param("asignadoEn", Timestamp.from(asignadoEn))
                .update() == 1;
    }

    boolean guardarEstado(
            Long pedidoId,
            Long repartidorId,
            EstadoEntrega estado,
            String llave,
            Instant registradoEn) {
        return jdbc.sql("""
                        INSERT INTO despacho_estado
                            (pedido_id, repartidor_id, estado, llave_estado, registrado_en)
                        VALUES
                            (:pedidoId, :repartidorId, :estado, :llave, :registradoEn)
                        ON CONFLICT (llave_estado) DO NOTHING
                        """)
                .param("pedidoId", pedidoId)
                .param("repartidorId", repartidorId)
                .param("estado", estado.name())
                .param("llave", llave)
                .param("registradoEn", Timestamp.from(registradoEn))
                .update() == 1;
    }

    void actualizarEstado(Long pedidoId, EstadoEntrega estado, Instant actualizadoEn) {
        int filas = jdbc.sql("""
                        UPDATE despacho_pedido
                        SET estado = :estado, actualizado_en = :actualizadoEn
                        WHERE pedido_id = :pedidoId
                        """)
                .param("pedidoId", pedidoId)
                .param("estado", estado.name())
                .param("actualizadoEn", Timestamp.from(actualizadoEn))
                .update();
        if (filas != 1) {
            throw new IllegalStateException("El pedido no tiene un despacho asignado");
        }
    }

    Optional<Long> repartidorDe(Long pedidoId) {
        return jdbc.sql("""
                        SELECT repartidor_id
                        FROM despacho_pedido
                        WHERE pedido_id = :pedidoId
                        """)
                .param("pedidoId", pedidoId)
                .query(Long.class)
                .optional();
    }

    Optional<EstadoEntrega> estadoDe(Long pedidoId) {
        return jdbc.sql("""
                        SELECT estado
                        FROM despacho_pedido
                        WHERE pedido_id = :pedidoId
                        """)
                .param("pedidoId", pedidoId)
                .query(String.class)
                .optional()
                .map(EstadoEntrega::valueOf);
    }
}
