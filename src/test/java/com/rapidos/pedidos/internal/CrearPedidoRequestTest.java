package com.rapidos.pedidos.internal;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrearPedidoRequestTest {

    private static final Validator validador =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void pedidoValidoNoTieneErrores() {
        var pedido = new CrearPedidoRequest(1L, 10L, List.of(new LineaPedidoRequest(1L, 2)));

        assertThat(camposConError(pedido)).isEmpty();
    }

    @Test
    void exigeRestauranteId() {
        var pedido = new CrearPedidoRequest(null, 10L, List.of(new LineaPedidoRequest(1L, 2)));

        assertThat(camposConError(pedido)).containsExactly("restauranteId");
    }

    @Test
    void exigeClienteId() {
        var pedido = new CrearPedidoRequest(1L, null, List.of(new LineaPedidoRequest(1L, 2)));

        assertThat(camposConError(pedido)).containsExactly("clienteId");
    }

    @Test
    void rechazaListaDeItemsVacia() {
        var pedido = new CrearPedidoRequest(1L, 10L, List.of());

        assertThat(camposConError(pedido)).containsExactly("items");
    }

    @Test
    void rechazaListaDeItemsNula() {
        var pedido = new CrearPedidoRequest(1L, 10L, null);

        assertThat(camposConError(pedido)).containsExactly("items");
    }

    @Test
    void rechazaCantidadCero() {
        var pedido = new CrearPedidoRequest(1L, 10L, List.of(new LineaPedidoRequest(1L, 0)));

        assertThat(camposConError(pedido)).containsExactly("items[0].cantidad");
    }

    @Test
    void rechazaCantidadNegativa() {
        var pedido = new CrearPedidoRequest(1L, 10L, List.of(new LineaPedidoRequest(1L, -1)));

        assertThat(camposConError(pedido)).containsExactly("items[0].cantidad");
    }

    @Test
    void exigeItemIdEnCadaLinea() {
        var pedido = new CrearPedidoRequest(1L, 10L, List.of(new LineaPedidoRequest(null, 2)));

        assertThat(camposConError(pedido)).containsExactly("items[0].itemId");
    }

    /** Devuelve los nombres de los campos que rompieron alguna regla. */
    private static List<String> camposConError(CrearPedidoRequest pedido) {
        return validador.validate(pedido).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .toList();
    }
}
