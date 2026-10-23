package com.rapidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.rapidos.despacho.aplicacion.puertos.entrada.ConsultarRepartidorUseCase;

/**
 * RAMAS §7 pasos 2–4: {@code POST /api/pedidos} → evento → despacho asigna una vez.
 * Perfil {@code test} usa H2 en memoria; no llama al menú por HTTP para crear el pedido.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoFlujoIT {

    private static final String BODY =
            """
            {"restauranteId":1,"clienteId":10,"items":[{"itemId":1,"cantidad":2}]}
            """;

    @Autowired
    MockMvc mvc;

    @Autowired
    ConsultarRepartidorUseCase despacho;

    @Test
    void postConfirmaPedidoYDespachoAsignaRepartidor() throws Exception {
        MvcResult creado = mvc.perform(post("/api/pedidos")
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "flujo-it-001")
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(24000.00))
                .andReturn();

        JsonNode json = new ObjectMapper().readTree(creado.getResponse().getContentAsString());
        long pedidoId = json.get("id").asLong();

        mvc.perform(post("/api/pedidos")
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "flujo-it-001")
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(pedidoId));

        esperarAsignacion(pedidoId);

        assertThat(despacho.repartidorDe(pedidoId)).isPresent();
    }

    private void esperarAsignacion(long pedidoId) throws InterruptedException {
        for (int intento = 0; intento < 100; intento++) {
            Optional<Long> repartidor = despacho.repartidorDe(pedidoId);
            if (repartidor.isPresent()) {
                return;
            }
            TimeUnit.MILLISECONDS.sleep(50);
        }
        assertThat(despacho.repartidorDe(pedidoId)).as("despacho tras PedidoConfirmado").isPresent();
    }
}
