package com.rapidos.ubicacion.infraestructura.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rapidos.ubicacion.aplicacion.puertos.entrada.RegistrarMedidaConfirmacionUseCase;
import com.rapidos.ubicacion.aplicacion.puertos.entrada.ReportarUbicacionUseCase;
import com.rapidos.ubicacion.aplicacion.puertos.salida.RepartidorDePedido;
import com.rapidos.ubicacion.aplicacion.puertos.salida.Reloj;
import com.rapidos.ubicacion.aplicacion.puertos.salida.UbicacionStore;
import com.rapidos.ubicacion.aplicacion.servicios.DegradacionGpsService;
import com.rapidos.ubicacion.aplicacion.servicios.UbicacionService;
import com.rapidos.ubicacion.dominio.ReglaDegradacionGps;

@Configuration
@EnableConfigurationProperties(UbicacionProperties.class)
class UbicacionConfig {

    @Bean
    ReglaDegradacionGps reglaDegradacionGps(UbicacionProperties propiedades) {
        return new ReglaDegradacionGps(propiedades.getVentanaMedidas(), propiedades.getMinimoMediciones());
    }

    @Bean
    RegistrarMedidaConfirmacionUseCase registrarMedidaConfirmacion(ReglaDegradacionGps regla) {
        return new DegradacionGpsService(regla);
    }

    @Bean
    ReportarUbicacionUseCase reportarUbicacion(
            RepartidorDePedido repartidores,
            ReglaDegradacionGps regla,
            UbicacionStore almacen,
            Reloj reloj) {
        return new UbicacionService(repartidores, regla, almacen, reloj);
    }
}
