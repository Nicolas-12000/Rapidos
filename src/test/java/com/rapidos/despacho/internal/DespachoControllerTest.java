package com.rapidos.despacho.internal;

import com.rapidos.despacho.DespachoApi;
import com.rapidos.despacho.EstadoEntrega;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DespachoController.class)
class DespachoControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DespachoApi despacho;

    @Test
    void registraElEstadoDelRepartidor() throws Exception {
        mvc.perform(post("/api/repartidores/5/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pedidoId":1,"estado":"RECOGIDO","llave":"est-1-recogido"}
                                """))
                .andExpect(status().isNoContent());

        verify(despacho).registrarEstado(5L, 1L, EstadoEntrega.RECOGIDO, "est-1-recogido");
    }

    @Test
    void rechazaUnaLlaveVacia() throws Exception {
        mvc.perform(post("/api/repartidores/5/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pedidoId":1,"estado":"RECOGIDO","llave":" "}
                                """))
                .andExpect(status().isBadRequest());
    }
}
