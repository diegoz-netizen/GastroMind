-- ============================================================
-- Migración 001 (13/10/2026): HU-16 y HU-22
-- Para una BD ya creada con una versión anterior de script_database.sql.
-- Una BD nueva NO la necesita: script_database.sql ya trae estos cambios.
-- Se puede correr más de una vez sin error (IF NOT EXISTS / OR REPLACE).
--
-- Uso:  psql -U postgres -d gastromind_demo -f docs/migraciones/001_vencimiento_y_proveedor_insumo.sql
-- ============================================================

BEGIN;

-- HU-16: fecha de vencimiento por lote (NULL = no perecible)
ALTER TABLE registro_ingreso_proveedor ADD COLUMN IF NOT EXISTS fecha_vencimiento DATE;
CREATE INDEX IF NOT EXISTS idx_ingreso_vencimiento ON registro_ingreso_proveedor(fecha_vencimiento);

-- HU-22: precio y tiempo de entrega de cada insumo por proveedor
CREATE TABLE IF NOT EXISTS proveedor_insumo (
    id                  SERIAL PRIMARY KEY,
    proveedor_id        INT NOT NULL REFERENCES proveedor(id),
    ingrediente_id      INT NOT NULL REFERENCES ingrediente(id),
    precio_unitario     DECIMAL(10,2) NOT NULL CHECK (precio_unitario > 0),
    tiempo_entrega_dias INT NOT NULL CHECK (tiempo_entrega_dias >= 0),
    UNIQUE (proveedor_id, ingrediente_id)
);
CREATE INDEX IF NOT EXISTS idx_proveedor_insumo_ingrediente ON proveedor_insumo(ingrediente_id);

CREATE OR REPLACE FUNCTION fn_valida_proveedor_insumo_mismo_restaurante()
RETURNS TRIGGER AS $$
DECLARE
    restaurante_proveedor INT;
    restaurante_ingrediente INT;
BEGIN
    SELECT restaurante_id INTO restaurante_proveedor
    FROM proveedor WHERE id = NEW.proveedor_id;

    SELECT restaurante_id INTO restaurante_ingrediente
    FROM ingrediente WHERE id = NEW.ingrediente_id;

    IF restaurante_proveedor IS DISTINCT FROM restaurante_ingrediente THEN
        RAISE EXCEPTION 'El proveedor % y el insumo % pertenecen a restaurantes distintos', NEW.proveedor_id, NEW.ingrediente_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_proveedor_insumo_mismo_restaurante ON proveedor_insumo;
CREATE TRIGGER trg_proveedor_insumo_mismo_restaurante
    BEFORE INSERT OR UPDATE ON proveedor_insumo
    FOR EACH ROW EXECUTE FUNCTION fn_valida_proveedor_insumo_mismo_restaurante();

COMMIT;
