package com.rapidos.ubicacion.infraestructura.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class UbicacionPropertiesBindingTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(ConfigPrueba.class);

    @Test
    void enlazaParametrosGps() {
        contexto
                .withPropertyValues(
                        "rapidos.ubicacion.ttl-segundos=45",
                        "rapidos.ubicacion.ventana-medidas=30",
                        "rapidos.ubicacion.minimo-mediciones=7")
                .run(ctx -> {
                    UbicacionProperties props = ctx.getBean(UbicacionProperties.class);
                    assertThat(props.getTtlSegundos()).isEqualTo(45);
                    assertThat(props.getVentanaMedidas()).isEqualTo(30);
                    assertThat(props.getMinimoMediciones()).isEqualTo(7);
                });
    }

    @Configuration
    @EnableConfigurationProperties(UbicacionProperties.class)
    static class ConfigPrueba {
    }
}
