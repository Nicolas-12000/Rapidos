package com.rapidos.despacho.infraestructura.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.rapidos.despacho.aplicacion.puertos.entrada.ProcesarPedidoConfirmado;
import com.rapidos.despacho.aplicacion.puertos.entrada.RegistrarEstadoUseCase;
import com.rapidos.despacho.aplicacion.puertos.salida.Avisador;
import com.rapidos.despacho.aplicacion.puertos.salida.DespachoRepository;
import com.rapidos.despacho.aplicacion.puertos.salida.Reloj;
import com.rapidos.despacho.aplicacion.servicios.DespachoService;
import com.rapidos.despacho.dominio.EstadoEntrega;

@Configuration
class DespachoConfig {

    @Bean
    DespachoService despachoService(DespachoRepository repositorio, Avisador avisador, Reloj reloj) {
        return new DespachoService(repositorio, avisador, reloj);
    }

    @Bean
    @Primary
    ProcesarPedidoConfirmado procesarPedidoConfirmado(
            DespachoService servicio,
            PlatformTransactionManager transacciones) {
        var tx = new TransactionTemplate(transacciones);
        return pedido -> tx.executeWithoutResult(estado -> servicio.procesar(pedido));
    }

    @Bean
    @Primary
    RegistrarEstadoUseCase registrarEstado(
            DespachoService servicio,
            PlatformTransactionManager transacciones) {
        var tx = new TransactionTemplate(transacciones);
        return new RegistrarEstadoUseCase() {
            @Override
            public void registrarEstado(Long repartidorId, Long pedidoId, EstadoEntrega estado, String llave) {
                tx.executeWithoutResult(ignorado -> servicio.registrarEstado(repartidorId, pedidoId, estado, llave));
            }

            @Override
            public Optional<EstadoEntrega> estadoDe(Long pedidoId) {
                return servicio.estadoDe(pedidoId);
            }
        };
    }
}
