package com.rapidos.pedidos.infraestructura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarColaUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarMenuUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoComando;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoUseCase;
import com.rapidos.pedidos.aplicacion.puertos.salida.Cobro;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuPort;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuRepository;
import com.rapidos.pedidos.aplicacion.puertos.salida.PedidoRepository;
import com.rapidos.pedidos.aplicacion.puertos.salida.PublicacionesPendientes;
import com.rapidos.pedidos.aplicacion.puertos.salida.PublicadorDeEventos;
import com.rapidos.pedidos.aplicacion.puertos.salida.Reloj;
import com.rapidos.pedidos.aplicacion.servicios.ConsultarColaService;
import com.rapidos.pedidos.aplicacion.servicios.ConsultarMenuService;
import com.rapidos.pedidos.aplicacion.servicios.CrearPedidoService;
import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

@Configuration
class PedidosConfig {

    @Bean
    ConsultarMenuUseCase consultarMenu(MenuRepository menu) {
        return new ConsultarMenuService(menu);
    }

    @Bean
    ConsultarColaUseCase consultarCola(PublicacionesPendientes publicaciones) {
        return new ConsultarColaService(publicaciones);
    }

    @Bean
    CrearPedidoService crearPedidoService(
            MenuPort menu,
            PedidoRepository pedidos,
            Cobro cobro,
            PublicadorDeEventos eventos,
            Reloj reloj) {
        return new CrearPedidoService(menu, pedidos, cobro, eventos, reloj);
    }

    @Bean
    @Primary
    CrearPedidoUseCase crearPedido(CrearPedidoService servicio, PlatformTransactionManager transacciones) {
        var tx = new TransactionTemplate(transacciones);
        return (CrearPedidoComando comando, String llave) -> {
            PedidoResumen resumen = tx.execute(estado -> servicio.crear(comando, llave));
            if (resumen == null) {
                throw new IllegalStateException("La confirmación del pedido no devolvió resultado");
            }
            return resumen;
        };
    }

    @Bean
    MenuPort menuPort(ConsultarMenuUseCase menu) {
        return new MenuPortAdaptador(menu);
    }
}
