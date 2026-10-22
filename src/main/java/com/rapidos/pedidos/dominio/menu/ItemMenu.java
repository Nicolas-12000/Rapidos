package com.rapidos.pedidos.dominio.menu;

import java.math.BigDecimal;


public class ItemMenu{

    private final String id;
    private final String restauranteId;
    private final String nombre;
    private final BigDecimal precio;
    private boolean disponible;

        
    public ItemMenu(String id, String restauranteId, String nombre,  BigDecimal precio, boolean disponible){
        this. id = id;
        this.restauranteId = restauranteId;
        this.nombre = nombre;
        this.precio = precio;
        this.disponible = disponible;
    }


    public static void platoDisponible(String id, String restauranteId, BigDecimal precio, boolean disponible){
        if(!disponible){
            throw new ReglaDeNegocioVioladaException("Los sentimos el plato no esta disponible" + restauranteId);
        } 
    }
    
    public static ItemMenu crear(String id, String nombre, String restauranteId, boolean disponible, BigDecimal precio){
        
        if(nombre == null || nombre.isBlank()){
            throw new ReglaDeNegocioVioladaException("El plato debe tener un nombre valido");
        } 

        if(precio == null || precio.signum() <= 0){
            throw new ReglaDeNegocioVioladaException("El precio ingresado no es valido debe ser mayor a 0");
        }
        return new ItemMenu(id, restauranteId, nombre, precio, disponible);
    }

    public boolean perteneceAlRestaurante(String otrorestauranteId){
        return this.restauranteId.equals(otrorestauranteId);
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

