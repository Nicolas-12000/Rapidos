package com.rapidos;

import com.rapidos.catalogo.CatalogoApi;
import com.rapidos.catalogo.ItemMenu;
import com.rapidos.pedidos.PedidoConfirmado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ApplicationModuleTest
class PedidoFlujoIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("rapidos")
            .withUsername("rapidos")
            .withPassword("rapidos");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockBean
    CatalogoApi catalogo;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AssertablePublishedEvents eventos;

    @BeforeEach
    void menuDePrueba() {
        var arepa = new ItemMenu(1L, 1L, "Arepa reina pepiada", new BigDecimal("12000.00"));
        when(catalogo.restauranteActivo(eq(1L))).thenReturn(true);
        when(catalogo.obtenerItem(eq(1L), eq(1L))).thenReturn(Optional.of(arepa));
    }

    @Test
    void crearPedido() throws Exception {
        var body = """
                {"restauranteId":1,"clienteId":10,"items":[{"itemId":1,"cantidad":2}]}
                """;

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "pedido-it-001")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.total").value(24000.0))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "pedido-it-001")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        var confirmados = eventos.ofType(PedidoConfirmado.class)
                .matching(PedidoConfirmado::pedidoId, 1L)
                .matching(PedidoConfirmado::restauranteId, 1L);
        assertThat(confirmados).hasSize(1);

        mockMvc.perform(get("/api/cola"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendientes").isNumber())
                .andExpect(jsonPath("$.publicaciones").isArray());
    }
}
