package com.rapidos.pedidos.dominio.menu;

public class Restaurante {
    private final Long id;
    private String nombre;
    private boolean activo;

    public Restaurante(Long id, String nombre, boolean activo ){
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
    }

    public void validarRestaurante(){
        if(!activo){
            throw new ReglaDeNegocioVioladaException("El " + nombre +"del resturante es incorrecto ingse un nombre valido");
        }
    }
}
