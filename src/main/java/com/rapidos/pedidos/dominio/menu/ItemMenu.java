package com.rapidos.pedidos.dominio.menu;

import java.math.BigDecimal;
/** 
@param id            identificador del plato 
@param restauranteId restaurante al que pertenece el plato
@param nombre        nombre del plato
@param precio        el precio unitario del plato
@param disponible   si el plato esta agotado o no. Si es {@code false}, el plato no se puede pedir.
*/
public record ItemMenu(
        Long id, 
        Long restauranteId, 
        String nombre, 
        BigDecimal precio, 
        boolean disponible) {

     /**
     * Constructor de compatibilidad: crea un plato disponible.
     * Lo usan las pruebas existentes de pedidos, que no conocen el campo {@code disponible}.
     */

    public ItemMenu(Long id, Long restauranteId, String nombre, BigDecimal precio){
        this(id, restauranteId, nombre, precio, true);
    }
        

        

}

