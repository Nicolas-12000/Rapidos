package com.rapidos.pedidos.internal;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
class CobroSimulado {

    boolean cobrar(Long clienteId, BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            return false;
        }
        return clienteId != null && clienteId > 0;
    }
}
