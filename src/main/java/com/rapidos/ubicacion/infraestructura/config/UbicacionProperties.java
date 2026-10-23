package com.rapidos.ubicacion.infraestructura.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Parámetros del dominio GPS (TTL, degradación). No elige adaptador de persistencia. */
@ConfigurationProperties(prefix = "rapidos.ubicacion")
public class UbicacionProperties {

    private long ttlSegundos = 30;
    private int ventanaMedidas = 20;
    private int minimoMediciones = 5;

    public long getTtlSegundos() {
        return ttlSegundos;
    }

    public void setTtlSegundos(long ttlSegundos) {
        this.ttlSegundos = ttlSegundos;
    }

    public int getVentanaMedidas() {
        return ventanaMedidas;
    }

    public void setVentanaMedidas(int ventanaMedidas) {
        this.ventanaMedidas = ventanaMedidas;
    }

    public int getMinimoMediciones() {
        return minimoMediciones;
    }

    public void setMinimoMediciones(int minimoMediciones) {
        this.minimoMediciones = minimoMediciones;
    }
}
