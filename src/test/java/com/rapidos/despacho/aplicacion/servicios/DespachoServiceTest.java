package com.rapidos.despacho.aplicacion.servicios;

import com.rapidos.despacho.aplicacion.puertos.salida.Avisador;
import com.rapidos.despacho.aplicacion.puertos.salida.DespachoRepository;
import com.rapidos.despacho.dominio.EstadoEntrega;
import com.rapidos.despacho.dominio.LlaveEstado;
import com.rapidos.pedidos.PedidoConfirmado;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba pura del servicio: sin Spring, sin Mockito, solo dobles en memoria.
 * Mismo estilo que ReservarBoletasServiceTest en carnaval-tickets-st.
 */
class DespachoServiceTest {

    // ------------------------------------------------------------------ dobles
    static class RepositorioFalso implements DespachoRepository {

        /** pedidoId → repartidorId asignado */
        final Map<Long, Long> asignaciones = new HashMap<>();
        /** pedidoId → EstadoEntrega actual */
        final Map<Long, EstadoEntrega> estados = new HashMap<>();
        /** llaves ya usadas — garantiza idempotencia (ADR-003) */
        final Set<String> llavesUsadas = new HashSet<>();
        /** historial de estados guardados */
        final List<String> historialEstados = new ArrayList<>();

        @Override
        public boolean crearAsignacion(Long pedidoId, Long repartidorId, Instant asignadoEn) {
            if (asignaciones.containsKey(pedidoId)) {
                return false; // ya existe → ON CONFLICT DO NOTHING
            }
            asignaciones.put(pedidoId, repartidorId);
            estados.put(pedidoId, EstadoEntrega.ASIGNADO);
            return true;
        }

        @Override
        public boolean guardarEstado(Long pedidoId, Long repartidorId,
                EstadoEntrega estado, String llave, Instant registradoEn) {
            if (llavesUsadas.contains(llave)) {
                return false; // llave duplicada → ON CONFLICT DO NOTHING
            }
            llavesUsadas.add(llave);
            historialEstados.add(llave);
            return true;
        }

        @Override
        public void actualizarEstado(Long pedidoId, EstadoEntrega estado, Instant actualizadoEn) {
            if (!asignaciones.containsKey(pedidoId)) {
                throw new IllegalStateException("El pedido no tiene un despacho asignado");
            }
            estados.put(pedidoId, estado);
        }

        @Override
        public Optional<Long> repartidorDe(Long pedidoId) {
            return Optional.ofNullable(asignaciones.get(pedidoId));
        }

        @Override
        public Optional<EstadoEntrega> estadoDe(Long pedidoId) {
            return Optional.ofNullable(estados.get(pedidoId));
        }
    }

    static class AvisadorFalso implements Avisador {
        final List<String> avisos = new ArrayList<>();

        @Override
        public void avisarRestaurante(PedidoConfirmado pedido) {
            avisos.add("restaurante:" + pedido.restauranteId());
        }

        @Override
        public void avisarRepartidor(Long repartidorId, PedidoConfirmado pedido) {
            avisos.add("repartidor:" + repartidorId);
        }
    }

    // ------------------------------------------------------------------ campos
    final RepositorioFalso repositorio = new RepositorioFalso();
    final AvisadorFalso avisador = new AvisadorFalso();
    final DespachoService servicio = new DespachoService(
            repositorio, avisador, () -> Instant.parse("2027-01-01T10:00:00Z"));

    final PedidoConfirmado pedido = new PedidoConfirmado(42L, 1L, new BigDecimal("15000.00"));

    // ------------------------------------------------------------------ tests

    @Test
    void procesarPedidoConfirmadoAsignaRepartidorYAvisa() {
        servicio.procesar(pedido);

        // repartidor asignado: floorMod(42-1, 4)+1 = floorMod(41,4)+1 = 1+1 = 2
        assertEquals(Optional.of(2L), repositorio.repartidorDe(42L));
        assertEquals(Optional.of(EstadoEntrega.ASIGNADO), repositorio.estadoDe(42L));
        assertTrue(avisador.avisos.contains("restaurante:1"));
        assertTrue(avisador.avisos.contains("repartidor:2"));
    }

    @Test
    void procesarDosVecesElMismoPedidoNoDuplica() {
        servicio.procesar(pedido);
        servicio.procesar(pedido); // reintento del mismo evento

        // sigue habiendo solo una asignación
        assertEquals(1, repositorio.asignaciones.size());
        // el segundo intento no volvió a avisar
        assertEquals(2, avisador.avisos.size());
    }

    @Test
    void registrarEstadoGuardaYActualiza() {
        servicio.procesar(pedido);

        servicio.registrarEstado(2L, 42L, EstadoEntrega.RECOGIDO, "llave-unica-1");

        assertEquals(Optional.of(EstadoEntrega.RECOGIDO), repositorio.estadoDe(42L));
        assertEquals(1, repositorio.historialEstados.size());
    }

    @Test
    void laMismaLlaveNoSeProcesaDosVeces() {
        servicio.procesar(pedido);

        servicio.registrarEstado(2L, 42L, EstadoEntrega.RECOGIDO, "llave-unica-2");
        // segunda vez con la misma llave → debe ignorarse (ADR-003)
        servicio.registrarEstado(2L, 42L, EstadoEntrega.ENTREGADO, "llave-unica-2");

        // el estado no avanzó a ENTREGADO porque la llave ya existía
        assertEquals(Optional.of(EstadoEntrega.RECOGIDO), repositorio.estadoDe(42L));
        assertEquals(1, repositorio.historialEstados.size());
    }

    @Test
    void llaveNulaLanzaExcepcionDesdeElDominio() {
        servicio.procesar(pedido);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarEstado(2L, 42L, EstadoEntrega.RECOGIDO, null));
    }

    @Test
    void llaveVaciaLanzaExcepcionDesdeElDominio() {
        servicio.procesar(pedido);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarEstado(2L, 42L, EstadoEntrega.RECOGIDO, "   "));
    }

    @Test
    void registrarEstadoConRepartidorEquivocadoFalla() {
        servicio.procesar(pedido); // asigna repartidor 2

        assertThrows(IllegalStateException.class,
                () -> servicio.registrarEstado(99L, 42L, EstadoEntrega.RECOGIDO, "llave-x"));
    }

    @Test
    void registrarEstadoSinAsignacionPreviaFalla() {
        // nunca se llamó procesar() para el pedido 999
        assertThrows(IllegalStateException.class,
                () -> servicio.registrarEstado(1L, 999L, EstadoEntrega.RECOGIDO, "llave-y"));
    }

    // ------------------------------------------------------------------ LlaveEstado

    @Test
    void llaveEstadoRechazaNulaYVacia() {
        assertThrows(IllegalArgumentException.class, () -> LlaveEstado.de(null));
        assertThrows(IllegalArgumentException.class, () -> LlaveEstado.de(""));
        assertThrows(IllegalArgumentException.class, () -> LlaveEstado.de("   "));
    }

    @Test
    void llaveEstadoRecortaEspacios() {
        LlaveEstado llave = LlaveEstado.de("  abc  ");
        assertEquals("abc", llave.valor());
    }
}
