package com.rapidos.ubicacion;

import java.time.Instant;

public record UbicacionActual(Long repartidorId, double lat, double lon, Instant hora) {
}
