package com.rapidos.pedidos.dominio.menu;

import java.math.BigDecimal;

public class ItemMenu{

    private final String id;
    private final String restauranteId;
    private final BigDecimal precio;
    private final boolean disponible;

        
    public ItemMenu(String id, String restauranteId, BigDecimal precio, boolean disponible){
        this. id = id;
        this.restauranteId = restauranteId;
        this.precio = precio;
        this.disponible = disponible;
    }

    public static void verMenu(String id, String restauranteId, BigDecimal precio, boolean disponible){
        if(disponible == true){
            // Mostrar información del plato
        } else {
            throw new ReglaDeNegocioVioladaException("Los sentimos el plato no esta disponible" + restauranteId

            );
        }
    }  

    public String getId(){
        return id;
    }

    public String getRestauranteId(){
        return restauranteId;
    }

    public BigDecimal getPrecio(){
        return precio;
    }

    public boolean isDisponible(){
        return disponible;
    }




    
        

        

}

