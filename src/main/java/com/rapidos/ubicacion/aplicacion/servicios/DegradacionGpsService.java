package com.rapidos.ubicacion.aplicacion.servicios;

import com.rapidos.ubicacion.aplicacion.puertos.entrada.RegistrarMedidaConfirmacionUseCase;
import com.rapidos.ubicacion.dominio.ReglaDegradacionGps;

public class DegradacionGpsService implements RegistrarMedidaConfirmacionUseCase {

    private final ReglaDegradacionGps regla;

    public DegradacionGpsService(ReglaDegradacionGps regla) {
        this.regla = regla;
    }

    @Override
    public void registrar(long duracionMs) {
        if (duracionMs < 0) {
            return;
        }
        regla.registrar(duracionMs);
    }
}
