package com.rapidos.ubicacion.infraestructura.entrada;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.rapidos.pedidos.ConfirmacionMedida;
import com.rapidos.ubicacion.dominio.ReglaDegradacionGps;

@Component
class ConfirmacionMedidaListener {

    private final ReglaDegradacionGps regla;

    ConfirmacionMedidaListener(ReglaDegradacionGps regla) {
        this.regla = regla;
    }

    @ApplicationModuleListener
    void alConfirmar(ConfirmacionMedida medida) {
        if (medida == null) {
            return;
        }
        regla.registrar(medida.duracionMs());
    }
}
