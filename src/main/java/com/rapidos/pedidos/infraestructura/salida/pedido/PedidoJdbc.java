package com.rapidos.pedidos.infraestructura.salida.pedido;

import com.rapidos.pedidos.aplicacion.puertos.salida.PedidoRepository;
import com.rapidos.pedidos.dominio.pedido.PedidoResumen;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Repository
class PedidoJdbc implements PedidoRepository {

    private final JdbcTemplate jdbc;

    public PedidoJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<PedidoResumen> porLlave(String llave) {
        return jdbc.query(
                """
                SELECT id, restaurante_id, cliente_id, total, estado, creado_en
                FROM pedido WHERE llave_pedido = ?
                """,
                rs -> rs.next()
                        ? Optional.of(mapResumen(rs))
                        : Optional.empty(),
                llave);
    }

    @Override
    public Optional<PedidoResumen> porId(Long pedidoId) {
        return jdbc.query(
                """
                SELECT id, restaurante_id, cliente_id, total, estado, creado_en
                FROM pedido WHERE id = ?
                """,
                rs -> rs.next()
                        ? Optional.of(mapResumen(rs))
                        : Optional.empty(),
                pedidoId);
    }

    @Override
    public PedidoResumen insertar(
            Long restauranteId,
            Long clienteId,
            BigDecimal total,
            String estado,
            Instant creadoEn,
            String llavePedido) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO pedido (restaurante_id, cliente_id, total, estado, creado_en, llave_pedido)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, restauranteId);
            ps.setLong(2, clienteId);
            ps.setBigDecimal(3, total);
            ps.setString(4, estado);
            ps.setTimestamp(5, Timestamp.from(creadoEn));
            ps.setString(6, llavePedido);
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKey().longValue();
        return new PedidoResumen(id, restauranteId, clienteId, total, estado, creadoEn);
    }

    @Override
    public void insertarLinea(Long pedidoId, Long itemId, int cantidad, BigDecimal precioUnitario) {
        jdbc.update(
                """
                INSERT INTO pedido_linea (pedido_id, item_id, cantidad, precio_unitario)
                VALUES (?, ?, ?, ?)
                """,
                pedidoId,
                itemId,
                cantidad,
                precioUnitario);
    }

    private static PedidoResumen mapResumen(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new PedidoResumen(
                rs.getLong("id"),
                rs.getLong("restaurante_id"),
                rs.getLong("cliente_id"),
                rs.getBigDecimal("total"),
                rs.getString("estado"),
                rs.getTimestamp("creado_en").toInstant());
    }
}
