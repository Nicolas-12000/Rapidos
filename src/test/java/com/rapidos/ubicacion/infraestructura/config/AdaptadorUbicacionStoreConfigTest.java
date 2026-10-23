package com.rapidos.ubicacion.infraestructura.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.rapidos.ubicacion.aplicacion.puertos.salida.UbicacionStore;
import com.rapidos.ubicacion.infraestructura.salida.UbicacionEnMemoria;
import com.rapidos.ubicacion.infraestructura.salida.UbicacionFirebase;

class AdaptadorUbicacionStoreConfigTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AdaptadorUbicacionStoreConfig.class));

    @Test
    void memoriaPorOmision() {
        contexto.run(ctx -> {
            assertThat(ctx).hasSingleBean(UbicacionStore.class);
            assertThat(ctx.getBean(UbicacionStore.class)).isInstanceOf(UbicacionEnMemoria.class);
        });
    }

    @Test
    void memoriaExplicita() {
        contexto
                .withPropertyValues("rapidos.persistencia.ubicacion=memoria")
                .run(ctx -> assertThat(ctx.getBean(UbicacionStore.class)).isInstanceOf(UbicacionEnMemoria.class));
    }

    @Test
    void firebaseConUrl() {
        contexto
                .withPropertyValues(
                        "rapidos.persistencia.ubicacion=firebase",
                        "rapidos.ubicacion.firebase.url=https://demo.firebaseio.com")
                .run(ctx -> assertThat(ctx.getBean(UbicacionStore.class)).isInstanceOf(UbicacionFirebase.class));
    }

    @Test
    void firebaseSinUrlNoArranca() {
        assertThatThrownBy(() -> contexto
                        .withPropertyValues("rapidos.persistencia.ubicacion=firebase")
                        .run(ctx -> ctx.getBean(UbicacionStore.class)))
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }
}
