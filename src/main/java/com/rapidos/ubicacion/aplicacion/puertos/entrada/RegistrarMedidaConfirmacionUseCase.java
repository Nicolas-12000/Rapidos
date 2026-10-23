package com.rapidos.ubicacion.aplicacion.puertos.entrada;

/** Ajusta la degradación del GPS según la latencia de confirmar pedidos (evento {@code ConfirmacionMedida}). */
public interface RegistrarMedidaConfirmacionUseCase {

    void registrar(long duracionMs);
}
