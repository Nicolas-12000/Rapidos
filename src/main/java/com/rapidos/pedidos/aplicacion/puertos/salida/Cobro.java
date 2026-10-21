package com.rapidos.pedidos.aplicacion.puertos.salida;

import java.math.BigDecimal;

public interface Cobro {

    boolean cobrar(Long clienteId, BigDecimal total);
}
