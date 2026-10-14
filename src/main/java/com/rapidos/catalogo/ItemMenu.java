package com.rapidos.catalogo;

import java.math.BigDecimal;

public record ItemMenu(Long id, Long restauranteId, String nombre, BigDecimal precio) {
}
