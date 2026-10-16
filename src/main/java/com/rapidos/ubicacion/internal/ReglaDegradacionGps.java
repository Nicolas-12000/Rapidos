package com.rapidos.ubicacion.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import com.rapidos.pedidos.ConfirmacionMedida;

@Service
class ReglaDegradacionGps {

    private static final int FRECUENCIA_RAPIDA = 5;
    private static final int FRECUENCIA_MEDIA = 10;
    private static final int FRECUENCIA_LENTA = 15;

    private final int tamanoVentana;
    private final int minimoMediciones;
    private final Deque<Long> mediciones = new ArrayDeque<>();
    private volatile int frecuencia = FRECUENCIA_RAPIDA;

    @Autowired
    ReglaDegradacionGps(
            @Value("${rapidos.ubicacion.ventana-medidas:20}") Integer tamanoVentana,
            @Value("${rapidos.ubicacion.minimo-mediciones:5}") Integer minimoMediciones) {
        this(tamanoVentana.intValue(), minimoMediciones.intValue());
    }

    ReglaDegradacionGps(int tamanoVentana, int minimoMediciones) {
        if (tamanoVentana < 1 || minimoMediciones < 1 || minimoMediciones > tamanoVentana) {
            throw new IllegalArgumentException(
                    "La ventana y el mínimo de mediciones deben ser positivos, y el mínimo no puede superar la ventana");
        }
        this.tamanoVentana = tamanoVentana;
        this.minimoMediciones = minimoMediciones;
    }

    @ApplicationModuleListener
    public void alConfirmar(ConfirmacionMedida medida) {
        if (medida == null) {
            return;
        }
        registrar(medida.duracionMs());
    }

    public synchronized void registrar(long duracionMs) {
        mediciones.addLast(duracionMs);
        if (mediciones.size() > tamanoVentana) {
            mediciones.removeFirst();
        }
        if (mediciones.size() < minimoMediciones) {
            return;
        }

        long p95 = p95(mediciones);
        int nuevaFrecuencia = frecuencia;
        if (p95 > 1500 && frecuencia < FRECUENCIA_LENTA) {
            nuevaFrecuencia = frecuencia == FRECUENCIA_RAPIDA
                    ? FRECUENCIA_MEDIA
                    : FRECUENCIA_LENTA;
        } else if (p95 < 1000 && frecuencia != FRECUENCIA_RAPIDA) {
            nuevaFrecuencia = FRECUENCIA_RAPIDA;
        }

        if (nuevaFrecuencia != frecuencia) {
            frecuencia = nuevaFrecuencia;
            mediciones.clear();
        }
    }

    public int frecuenciaSugeridaSegundos() {
        return frecuencia;
    }

    private static long p95(Deque<Long> valores) {
        List<Long> ordenados = new ArrayList<>(valores);
        Collections.sort(ordenados);
        int indice = (int) Math.ceil(0.95 * ordenados.size()) - 1;
        return ordenados.get(indice);
    }
}
