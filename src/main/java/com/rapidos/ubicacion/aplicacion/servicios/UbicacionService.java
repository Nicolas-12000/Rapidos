package com.rapidos.ubicacion.aplicacion.servicios;

import java.util.Optional;

import com.rapidos.ubicacion.aplicacion.puertos.entrada.ReportarUbicacionUseCase;
import com.rapidos.ubicacion.aplicacion.puertos.salida.RepartidorDePedido;
import com.rapidos.ubicacion.aplicacion.puertos.salida.Reloj;
import com.rapidos.ubicacion.aplicacion.puertos.salida.UbicacionStore;
import com.rapidos.ubicacion.dominio.ReglaDegradacionGps;
import com.rapidos.ubicacion.dominio.UbicacionActual;

public class UbicacionService implements ReportarUbicacionUseCase {

    private final RepartidorDePedido repartidores;
    private final ReglaDegradacionGps reglaDegradacion;
    private final UbicacionStore almacen;
    private final Reloj reloj;

    public UbicacionService(
            RepartidorDePedido repartidores,
            ReglaDegradacionGps reglaDegradacion,
            UbicacionStore almacen,
            Reloj reloj) {
        this.repartidores = repartidores;
        this.reglaDegradacion = reglaDegradacion;
        this.almacen = almacen;
        this.reloj = reloj;
    }

    @Override
    public void reportar(Long repartidorId, double lat, double lon) {
        almacen.guardar(new UbicacionActual(repartidorId, lat, lon, reloj.ahora()));
    }

    @Override
    public Optional<UbicacionActual> ultimaDePedido(Long pedidoId) {
        return repartidores.repartidorDe(pedidoId).flatMap(almacen::ultimaDe);
    }

    @Override
    public int frecuenciaSugeridaSegundos() {
        return reglaDegradacion.frecuenciaSugeridaSegundos();
    }
}
