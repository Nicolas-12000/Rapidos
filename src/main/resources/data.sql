-- Datos de ejemplo. Usa INSERT ... ON CONFLICT DO NOTHING para poder re-ejecutar.
SELECT 1;

-- ===== menú (dentro de pedidos) =====
INSERT INTO restaurante (id, nombre, activo) VALUES
    (1, 'Arepas de la 19',     TRUE),
    (2, 'Pizzería La Plaza',   TRUE),
    (3, 'Asadero Cerrado',     FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO item_menu (id, restaurante_id, nombre, precio, disponible) VALUES
    (1, 1, 'Arepa reina pepiada',      12000.00, TRUE),
    (2, 1, 'Jugo natural',              5000.00, TRUE),
    (3, 1, 'Arepa de chócolo con queso', 9000.00, FALSE),
    (4, 2, 'Pizza hawaiana mediana',   32000.00, TRUE),
    (5, 2, 'Pizza pepperoni mediana',  34000.00, TRUE)
ON CONFLICT (id) DO NOTHING;

-- ===== pedidos (Dev 1 + Dev 5) =====
-- Sin datos iniciales: los pedidos se crean por API (POST /api/pedidos).