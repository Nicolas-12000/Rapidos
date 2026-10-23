package com.rapidos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulosTest {

    private final ApplicationModules modulos = ApplicationModules.of(RapidosApplication.class);

    @Test
    void verificaLasFronterasDeLosModulos() {
        modulos.verify();
    }

    @Test
    void soloHayTresModulos() {
        assertThat(modulos.stream().map(modulo -> modulo.getIdentifier().toString()))
                .containsExactlyInAnyOrder("pedidos", "despacho", "ubicacion");
    }

    @Test
    void generaDocumentacion() {
        new Documenter(modulos).writeDocumentation();
    }
}
