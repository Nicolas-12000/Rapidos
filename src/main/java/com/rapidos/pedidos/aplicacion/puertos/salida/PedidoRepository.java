package com.rapidos.pedidos.aplicacion.puertos.salida;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

public interface PedidoRepository {

    Optional<PedidoResumen> porLlave(String llave);

    Optional<PedidoResumen> porId(Long pedidoId);

    PedidoResumen insertar(
            Long restauranteId,
            Long clienteId,
            BigDecimal total,
            String estado,
            Instant creadoEn,
            String llavePedido);

    void insertarLinea(Long pedidoId, Long itemId, int cantidad, BigDecimal precioUnitario);
}
