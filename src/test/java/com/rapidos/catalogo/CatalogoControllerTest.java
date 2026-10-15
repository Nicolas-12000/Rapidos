package com.rapidos.catalogo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba el endpoint GET /api/restaurantes/{id}/menu (requiere docker compose up -d).
 */
@ApplicationModuleTest
@AutoConfigureMockMvc
class CatalogoControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void devuelveElMenuDelRestaurante1() throws Exception {
        mvc.perform(get("/api/restaurantes/1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nombre").value("Arepa reina pepiada"))
                .andExpect(jsonPath("$[0].disponible").value(true))
                .andExpect(jsonPath("$[2].disponible").value(false));
    }

    @Test
    void restauranteInexistenteDa404() throws Exception {
        mvc.perform(get("/api/restaurantes/999/menu"))
                .andExpect(status().isNotFound());
    }

    @Test
    void restauranteInactivoDa404() throws Exception {
        mvc.perform(get("/api/restaurantes/3/menu"))
                .andExpect(status().isNotFound());
    }
}