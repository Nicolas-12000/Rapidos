-- Cada dueño de módulo agrega SUS tablas en su sección.
-- Usa CREATE TABLE IF NOT EXISTS para que se pueda ejecutar varias veces.

-- ===== catalogo (Dev 2) =====

-- ===== pedidos (Dev 1 + Dev 5) =====

CREATE TABLE IF NOT EXISTS pedido (
    id               BIGSERIAL PRIMARY KEY,
    restaurante_id   BIGINT         NOT NULL,
    cliente_id       BIGINT         NOT NULL,
    total            NUMERIC(12, 2) NOT NULL,
    estado           VARCHAR(40)    NOT NULL,
    creado_en        TIMESTAMPTZ    NOT NULL,
    llave_pedido     VARCHAR(120)   NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS pedido_linea (
    id              BIGSERIAL PRIMARY KEY,
    pedido_id       BIGINT         NOT NULL REFERENCES pedido (id),
    item_id         BIGINT         NOT NULL,
    cantidad        INT            NOT NULL,
    precio_unitario NUMERIC(12, 2) NOT NULL
);

-- ===== despacho (Dev 3) =====

-- ===== ubicacion: no usa base de datos (solo memoria) =====
