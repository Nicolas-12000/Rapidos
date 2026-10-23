package com.rapidos.pedidos.infraestructura.salida.cobro;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import com.rapidos.pedidos.aplicacion.puertos.salida.Cobro;

@Component
class CobroSimulado implements Cobro {

    @Override
    public boolean cobrar(Long clienteId, BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            return false;
        }
        return clienteId != null && clienteId > 0;
    }
}
