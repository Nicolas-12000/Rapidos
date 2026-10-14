package com.rapidos.pedidos.internal;

import com.rapidos.catalogo.CatalogoApi;
import com.rapidos.pedidos.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
class PedidosService implements PedidosApi {

    private final CatalogoApi catalogo;
    private final PedidoJdbc pedidos;
    private final CobroSimulado cobro;
    private final ApplicationEventPublisher eventos;

    PedidosService(
            CatalogoApi catalogo,
            PedidoJdbc pedidos,
            CobroSimulado cobro,
            ApplicationEventPublisher eventos) {
        this.catalogo = catalogo;
        this.pedidos = pedidos;
        this.cobro = cobro;
        this.eventos = eventos;
    }

    @Override
    @Transactional
    public PedidoResumen crear(CrearPedidoComando comando, String llavePedido) {
        long inicio = System.nanoTime();
        if (!StringUtils.hasText(llavePedido)) {
            throw new IllegalArgumentException("Falta la cabecera Idempotency-Key");
        }
        var existente = pedidos.porLlave(llavePedido.trim());
        if (existente.isPresent()) {
            return existente.get();
        }
        validarComando(comando);
        if (!catalogo.restauranteActivo(comando.restauranteId())) {
            throw new IllegalArgumentException("Restaurante no disponible");
        }
        BigDecimal total = calcularTotal(comando);
        if (!cobro.cobrar(comando.clienteId(), total)) {
            throw new IllegalStateException("Cobro rechazado");
        }
        var creadoEn = Instant.now();
        var resumen = pedidos.insertar(
                comando.restauranteId(),
                comando.clienteId(),
                total,
                "CONFIRMADO",
                creadoEn,
                llavePedido.trim());
        for (var linea : comando.items()) {
            var item = catalogo
                    .obtenerItem(comando.restauranteId(), linea.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("Ítem no válido: " + linea.itemId()));
            pedidos.insertarLinea(resumen.id(), linea.itemId(), linea.cantidad(), item.precio());
        }
        eventos.publishEvent(new PedidoConfirmado(resumen.id(), resumen.restauranteId(), resumen.total()));
        long duracionMs = (System.nanoTime() - inicio) / 1_000_000L;
        eventos.publishEvent(new ConfirmacionMedida(duracionMs));
        return resumen;
    }

    @Override
    public Optional<PedidoResumen> consultar(Long pedidoId) {
        return pedidos.porId(pedidoId);
    }

    private static void validarComando(CrearPedidoComando comando) {
        if (comando.restauranteId() == null || comando.clienteId() == null) {
            throw new IllegalArgumentException("restauranteId y clienteId son obligatorios");
        }
        if (comando.items() == null || comando.items().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un ítem");
        }
        for (var linea : comando.items()) {
            if (linea.itemId() == null || linea.cantidad() < 1) {
                throw new IllegalArgumentException("Cada línea requiere itemId y cantidad >= 1");
            }
        }
    }

    static BigDecimal calcularTotal(CrearPedidoComando comando, CatalogoApi catalogo) {
        validarComando(comando);
        return calcularTotalInterno(comando, catalogo);
    }

    private BigDecimal calcularTotal(CrearPedidoComando comando) {
        return calcularTotalInterno(comando, catalogo);
    }

    private static BigDecimal calcularTotalInterno(CrearPedidoComando comando, CatalogoApi catalogo) {
        BigDecimal total = BigDecimal.ZERO;
        for (var linea : comando.items()) {
            var item = catalogo
                    .obtenerItem(comando.restauranteId(), linea.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("Ítem no válido: " + linea.itemId()));
            total = total.add(item.precio().multiply(BigDecimal.valueOf(linea.cantidad())));
        }
        return total;
    }
}
