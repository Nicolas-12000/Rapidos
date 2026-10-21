package com.rapidos.despacho.aplicacion.servicios;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import com.rapidos.despacho.ConsultaRepartidor;
import com.rapidos.despacho.aplicacion.puertos.entrada.ProcesarPedidoConfirmado;
import com.rapidos.despacho.aplicacion.puertos.entrada.RegistrarEstadoUseCase;
import com.rapidos.despacho.aplicacion.puertos.salida.Avisador;
import com.rapidos.despacho.aplicacion.puertos.salida.DespachoRepository;
import com.rapidos.despacho.dominio.EstadoEntrega;
import com.rapidos.pedidos.PedidoConfirmado;

public class DespachoService implements ProcesarPedidoConfirmado, RegistrarEstadoUseCase, ConsultaRepartidor {

    private static final long REPARTIDORES_DEMO = 4;

    private final DespachoRepository repositorio;
    private final Avisador avisador;
    private final Clock reloj;

    public DespachoService(DespachoRepository repositorio, Avisador avisador, Clock reloj) {
        this.repositorio = repositorio;
        this.avisador = avisador;
        this.reloj = reloj;
    }

    @Override
    public void procesar(PedidoConfirmado pedido) {
        validarPedidoConfirmado(pedido);
        Long repartidorId = asignarRepartidor(pedido.pedidoId());

        if (!repositorio.crearAsignacion(pedido.pedidoId(), repartidorId, reloj.instant())) {
            return;
        }

        avisador.avisarRestaurante(pedido);
        avisador.avisarRepartidor(repartidorId, pedido);
    }

    @Override
    public void registrarEstado(Long repartidorId, Long pedidoId, EstadoEntrega estado, String llave) {
        validarEstado(repartidorId, pedidoId, estado, llave);

        Long asignado = repositorio.repartidorDe(pedidoId)
                .orElseThrow(() -> new IllegalStateException("El pedido no tiene un despacho asignado"));
        if (!asignado.equals(repartidorId)) {
            throw new IllegalStateException("El pedido pertenece a otro repartidor");
        }

        Instant ahora = reloj.instant();
        if (repositorio.guardarEstado(pedidoId, repartidorId, estado, llave.trim(), ahora)) {
            repositorio.actualizarEstado(pedidoId, estado, ahora);
        }
    }

    @Override
    public Optional<Long> repartidorDe(Long pedidoId) {
        if (pedidoId == null) {
            return Optional.empty();
        }
        return repositorio.repartidorDe(pedidoId);
    }

    @Override
    public Optional<EstadoEntrega> estadoDe(Long pedidoId) {
        if (pedidoId == null) {
            return Optional.empty();
        }
        return repositorio.estadoDe(pedidoId);
    }

    private static Long asignarRepartidor(Long pedidoId) {
        return Math.floorMod(pedidoId - 1, REPARTIDORES_DEMO) + 1;
    }

    private static void validarPedidoConfirmado(PedidoConfirmado pedido) {
        if (pedido == null || pedido.pedidoId() == null || pedido.restauranteId() == null) {
            throw new IllegalArgumentException("El pedido confirmado está incompleto");
        }
    }

    private static void validarEstado(
            Long repartidorId,
            Long pedidoId,
            EstadoEntrega estado,
            String llave) {
        if (repartidorId == null || pedidoId == null || estado == null || llave == null || llave.isBlank()) {
            throw new IllegalArgumentException("repartidorId, pedidoId, estado y llave son obligatorios");
        }
    }
}
