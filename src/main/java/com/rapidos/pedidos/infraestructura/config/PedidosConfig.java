package com.rapidos.pedidos.infraestructura.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarMenuUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoComando;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoUseCase;
import com.rapidos.pedidos.aplicacion.puertos.salida.Cobro;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuPort;
import com.rapidos.pedidos.aplicacion.puertos.salida.MenuRepository;
import com.rapidos.pedidos.aplicacion.puertos.salida.PedidoRepository;
import com.rapidos.pedidos.aplicacion.puertos.salida.PublicadorDeEventos;
import com.rapidos.pedidos.aplicacion.servicios.ConsultarMenuService;
import com.rapidos.pedidos.aplicacion.servicios.CrearPedidoService;
import com.rapidos.pedidos.dominio.menu.ItemMenu;
import com.rapidos.pedidos.dominio.pedido.PedidoResumen;

@Configuration
class PedidosConfig {

    @Bean
    ConsultarMenuUseCase consultarMenu(MenuRepository menu) {
        return new ConsultarMenuService(menu);
    }

    @Bean
    CrearPedidoService crearPedidoService(
            MenuPort menu,
            PedidoRepository pedidos,
            Cobro cobro,
            PublicadorDeEventos eventos) {
        return new CrearPedidoService(menu, pedidos, cobro, eventos);
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
        return new MenuPort() {
            @Override
            public boolean restauranteActivo(Long restauranteId) {
                return menu.restauranteActivo(restauranteId);
            }

            @Override
            public Optional<ItemMenu> obtenerItem(Long restauranteId, Long itemId) {
                return menu.obtenerItem(restauranteId, itemId);
            }
        };
    }
}
