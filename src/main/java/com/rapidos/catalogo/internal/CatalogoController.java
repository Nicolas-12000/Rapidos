package com.rapidos.catalogo.internal;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rapidos.catalogo.CatalogoApi;
import com.rapidos.catalogo.ItemMenu;

/**
 * Endpoint HTTP del menú
 */
@RestController
@RequestMapping("/api/restaurantes")
class CatalogoController {

    private final CatalogoApi catalogo;

    CatalogoController(CatalogoApi catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/{restauranteId}/menu")
    ResponseEntity<List<ItemMenu>> menu(@PathVariable Long restauranteId) {
        if (!catalogo.restauranteActivo(restauranteId)) {
            return ResponseEntity.notFound().build();   // 404
        }
        return ResponseEntity.ok(catalogo.menu(restauranteId));  // 200 + JSON
    }
}
