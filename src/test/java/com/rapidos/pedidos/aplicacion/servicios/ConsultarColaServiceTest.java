package com.rapidos.pedidos.aplicacion.servicios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.rapidos.pedidos.aplicacion.consultas.PublicacionPendiente;
import com.rapidos.pedidos.aplicacion.puertos.salida.PublicacionesPendientes;

class ConsultarColaServiceTest {

    private final PublicacionesPendientes publicaciones = Mockito.mock(PublicacionesPendientes.class);
    private final ConsultarColaService servicio = new ConsultarColaService(publicaciones);

    @Test
    void resumePublicacionesPendientes() {
        when(publicaciones.incompletas()).thenReturn(List.of(
                new PublicacionPendiente("1", "PedidoConfirmado", "despacho", "2026-01-01T00:00:00Z")));

        var resumen = servicio.resumen();

        assertThat(resumen.pendientes()).isEqualTo(1);
        assertThat(resumen.publicaciones()).hasSize(1);
        assertThat(resumen.publicaciones().get(0).evento()).isEqualTo("PedidoConfirmado");
    }
}
