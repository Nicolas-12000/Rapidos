package com.rapidos;

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
    void generaDocumentacion() {
        new Documenter(modulos).writeDocumentation();
    }
}
