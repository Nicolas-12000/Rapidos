package com.rapidos.pedidos.aplicacion.servicios;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import com.rapidos.pedidos.ConfirmacionMedida;
import com.rapidos.pedidos.PedidoConfirmado;
import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarPedidoUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoComando;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoUseCase;
import com.rapidos.pedidos.aplicacion.puertos.salida.Cobro;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuPort;
import com.rapidos.pedidos.aplicacion.puertos.salida.PedidoRepository;
import com.rapidos.pedidos.aplicacion.puertos.salida.PublicadorDeEventos;
import com.rapidos.pedidos.aplicacion.puertos.salida.Reloj;
import com.rapidos.pedidos.dominio.pedido.Pedido;
import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

public class CrearPedidoService implements CrearPedidoUseCase, ConsultarPedidoUseCase {

    private final MenuPort menu;
    private final PedidoRepository pedidos;
    private final Cobro cobro;
    private final PublicadorDeEventos eventos;
    private final Reloj reloj;

    public CrearPedidoService(
            MenuPort menu,
            PedidoRepository pedidos,
            Cobro cobro,
            PublicadorDeEventos eventos,
            Reloj reloj) {
        this.menu = menu;
        this.pedidos = pedidos;
        this.cobro = cobro;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    @Override
    public PedidoResumen crear(CrearPedidoComando comando, String llavePedido) {
        long inicio = reloj.nanos();
        if (llavePedido == null || llavePedido.isBlank()) {
            throw new IllegalArgumentException("Falta la cabecera Idempotency-Key");
        }
        var existente = pedidos.porLlave(llavePedido.trim());
        if (existente.isPresent()) {
            return existente.get();
        }
        if (comando == null || comando.restauranteId() == null || comando.clienteId() == null) {
            throw new IllegalArgumentException("restauranteId y clienteId son obligatorios");
        }
        if (comando.items() == null || comando.items().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un ítem");
        }
        if (!menu.restauranteActivo(comando.restauranteId())) {
            throw new IllegalArgumentException("Restaurante no disponible");
        }
        Pedido pedido = armar(comando);
        if (!cobro.cobrar(pedido.clienteId(), pedido.total())) {
            throw new IllegalStateException("Cobro rechazado");
        }
        var creadoEn = reloj.ahora();
        var resumen = pedidos.insertar(
                pedido.restauranteId(),
                pedido.clienteId(),
                pedido.total(),
                "CONFIRMADO",
                creadoEn,
                llavePedido.trim());
        var lineas = pedido.lineas();
        var precios = pedido.preciosUnitarios();
        for (int i = 0; i < lineas.size(); i++) {
            var linea = lineas.get(i);
            pedidos.insertarLinea(resumen.id(), linea.itemId(), linea.cantidad(), precios.get(i));
        }
        eventos.publicar(new PedidoConfirmado(resumen.id(), resumen.restauranteId(), resumen.total()));
        long duracionMs = (reloj.nanos() - inicio) / 1_000_000L;
        eventos.publicar(new ConfirmacionMedida(duracionMs));
        return resumen;
    }

    @Override
    public Optional<PedidoResumen> consultar(Long pedidoId) {
        return pedidos.porId(pedidoId);
    }

    private Pedido armar(CrearPedidoComando comando) {
        var precios = new ArrayList<BigDecimal>();
        for (var linea : comando.items()) {
            var item = menu
                    .obtenerItem(comando.restauranteId(), linea.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("Ítem no válido: " + linea.itemId()));
            if (!item.disponible()) {
                throw new IllegalArgumentException("Ítem no disponible: " + linea.itemId());
            }
            precios.add(item.precio());
        }
        return Pedido.confirmar(comando.restauranteId(), comando.clienteId(), comando.items(), precios);
    }
}
