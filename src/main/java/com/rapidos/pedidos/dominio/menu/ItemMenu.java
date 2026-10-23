package com.rapidos.pedidos.dominio.menu;
import java.math.BigDecimal;

public class ItemMenu{

    private final Long id;
    private final Long restauranteId;
    private final String nombre;
    private final BigDecimal precio;
    private boolean disponible;

        
    public ItemMenu(Long id, Long restauranteId, String nombre,  BigDecimal precio, boolean disponible){
        this. id = id;
        this.restauranteId = restauranteId;
        this.nombre = nombre;
        this.precio = precio;
        this.disponible = disponible;
    }


    public static void platoDisponible(Long id, Long restauranteId, BigDecimal precio, boolean disponible){
        if(!disponible){
            throw new ReglaDeNegocioVioladaException("Los sentimos el plato no esta disponible" + restauranteId);
        } 
    }
    
    public static ItemMenu crear(Long id, String nombre, Long restauranteId, boolean disponible, BigDecimal precio, Restaurante restaurante){
        restaurante.validarRestaurante();
        if(nombre == null || nombre.isBlank()){
            throw new ReglaDeNegocioVioladaException("El plato debe tener un nombre valido");
        } 

        if(precio == null || precio.signum() <= 0){
            throw new ReglaDeNegocioVioladaException("El precio ingresado no es valido debe ser mayor a 0");
        }
        return new ItemMenu(id, restauranteId, nombre, precio, disponible);
    }

    public boolean perteneceAlRestaurante(Long otrorestauranteId){
        return this.restauranteId.equals(otrorestauranteId);
    }

    public Long Id(){
        return id;
    }
    public Long RestauranteId(){
        return restauranteId;
    }

    public BigDecimal Precio(){
        return precio;
    }

    public boolean isDisponible(){
        return disponible;
    }




    
        

        

}

