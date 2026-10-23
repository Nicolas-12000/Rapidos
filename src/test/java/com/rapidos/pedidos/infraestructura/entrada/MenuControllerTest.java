package com.rapidos.pedidos.infraestructura.entrada;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarColaUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarMenuUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.ConsultarPedidoUseCase;
import com.rapidos.pedidos.aplicacion.puertos.entrada.CrearPedidoUseCase;
import com.rapidos.pedidos.dominio.menu.ItemMenu;

@WebMvcTest(PedidosController.class)
class MenuControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CrearPedidoUseCase crearPedido;

    @MockitoBean
    ConsultarPedidoUseCase consultarPedido;

    @MockitoBean
    ConsultarMenuUseCase menu;

    @MockitoBean
    ConsultarColaUseCase cola;

    @Test
    void devuelveElMenuDelRestaurante1() throws Exception {
        when(menu.menuSiActivo(1L)).thenReturn(Optional.of(List.of(
                new ItemMenu(1L, 1L, "Arepa reina pepiada", new BigDecimal("12000.00"), true),
                new ItemMenu(2L, 1L, "Jugo natural", new BigDecimal("5000.00"), true),
                new ItemMenu(3L, 1L, "Arepa de chócolo", new BigDecimal("9000.00"), false))));

        mvc.perform(get("/api/restaurantes/1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nombre").value("Arepa reina pepiada"))
                .andExpect(jsonPath("$[0].disponible").value(true))
                .andExpect(jsonPath("$[2].disponible").value(false));
    }

    @Test
    void restauranteInexistenteDa404() throws Exception {
        when(menu.menuSiActivo(999L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/restaurantes/999/menu"))
                .andExpect(status().isNotFound());
    }

    @Test
    void restauranteInactivoDa404() throws Exception {
        when(menu.menuSiActivo(3L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/restaurantes/3/menu"))
                .andExpect(status().isNotFound());
    }
}
