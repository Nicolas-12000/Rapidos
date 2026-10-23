package com.rapidos.pedidos.dominio.menu;

/**
 * ReglaDeNegocioVioladaException
 */
public class  ReglaDeNegocioVioladaException extends RuntimeException {
    public ReglaDeNegocioVioladaException(String mensaje) {
        super(mensaje);
    }
}
