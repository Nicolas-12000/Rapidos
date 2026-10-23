package com.rapidos.ubicacion.infraestructura.entrada;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.rapidos.pedidos.ConfirmacionMedida;
import com.rapidos.ubicacion.aplicacion.puertos.entrada.RegistrarMedidaConfirmacionUseCase;

@Component
class ConfirmacionMedidaListener {

    private final RegistrarMedidaConfirmacionUseCase degradacion;

    ConfirmacionMedidaListener(RegistrarMedidaConfirmacionUseCase degradacion) {
        this.degradacion = degradacion;
    }

    @ApplicationModuleListener
    void alConfirmar(ConfirmacionMedida medida) {
        if (medida == null) {
            return;
        }
        degradacion.registrar(medida.duracionMs());
    }
}
