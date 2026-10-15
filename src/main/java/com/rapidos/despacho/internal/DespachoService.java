package com.rapidos.despacho.internal;

import com.rapidos.despacho.DespachoApi;
import com.rapidos.despacho.EstadoEntrega;
import com.rapidos.pedidos.PedidoConfirmado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
class DespachoService implements DespachoApi {

    private static final long REPARTIDORES_DEMO = 4;

    private final DespachoRepository repositorio;
    private final AvisadorDespacho avisador;
    private final Clock reloj;

    @Autowired
    DespachoService(DespachoRepository repositorio, AvisadorDespacho avisador) {
        this(repositorio, avisador, Clock.systemUTC());
    }

    DespachoService(DespachoRepository repositorio, AvisadorDespacho avisador, Clock reloj) {
        this.repositorio = repositorio;
        this.avisador = avisador;
        this.reloj = reloj;
    }

    @Transactional
    void procesarConfirmacion(PedidoConfirmado pedido) {
        validarPedidoConfirmado(pedido);
        Long repartidorId = asignarRepartidor(pedido.pedidoId());

        if (!repositorio.crearAsignacion(pedido.pedidoId(), repartidorId, reloj.instant())) {
            return;
        }

        // Si alguno de los avisos falla, la transacción se revierte. La publicación
        // queda pendiente y Spring Modulith puede reintentarla sin duplicar el despacho.
        avisador.avisarRestaurante(pedido);
        avisador.avisarRepartidor(repartidorId, pedido);
    }

    @Override
    @Transactional
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
        if (repartidorId == null || pedidoId == null || estado == null || !StringUtils.hasText(llave)) {
            throw new IllegalArgumentException("repartidorId, pedidoId, estado y llave son obligatorios");
        }
    }
}
