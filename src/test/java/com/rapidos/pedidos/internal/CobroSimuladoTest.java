package com.rapidos.pedidos.internal;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CobroSimuladoTest {

    private final CobroSimulado cobro = new CobroSimulado();

    @Test
    void cobraCuandoElClienteYElTotalSonValidos() {
        assertThat(cobro.cobrar(10L, new BigDecimal("24000.00"))).isTrue();
    }

    @Test
    void rechazaUnTotalEnCero() {
        assertThat(cobro.cobrar(10L, BigDecimal.ZERO)).isFalse();
    }

    @Test
    void rechazaUnTotalNegativo() {
        assertThat(cobro.cobrar(10L, new BigDecimal("-1.00"))).isFalse();
    }

    @Test
    void rechazaUnTotalNulo() {
        assertThat(cobro.cobrar(10L, null)).isFalse();
    }

    @Test
    void rechazaUnClienteNulo() {
        assertThat(cobro.cobrar(null, new BigDecimal("24000.00"))).isFalse();
    }

    @Test
    void rechazaUnClienteConIdCeroONegativo() {
        assertThat(cobro.cobrar(0L, new BigDecimal("24000.00"))).isFalse();
        assertThat(cobro.cobrar(-5L, new BigDecimal("24000.00"))).isFalse();
    }
}
