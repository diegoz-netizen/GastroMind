-- ============================================================
-- Migración 002 (14/10/2026): Spring pasa a ddl-auto=validate (Planning 2, punto 4)
-- Para una BD ya creada. Una BD nueva NO la necesita: script_database.sql ya está al día.
-- Se puede correr más de una vez sin error.
--
-- 1. Crea historial_estado_empleado (HU-03) y token_recuperacion (HU-04) si no existen:
--    hasta ahora solo las creaba ddl-auto=update, no estaban en el script.
-- 2. Devuelve a INTEGER los id y FK que ddl-auto=update pasó a BIGINT,
--    para que la BD coincida con script_database.sql y con las entidades.
--
-- Uso:  psql -U postgres -d gastromind_demo -f docs/migraciones/002_tablas_spring_y_ids_integer.sql
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS historial_estado_empleado (
    id              SERIAL PRIMARY KEY,
    empleado_id     INT NOT NULL REFERENCES empleado(id),
    estado_anterior VARCHAR(20) NOT NULL,
    estado_nuevo    VARCHAR(20) NOT NULL,
    cambiado_por    INT NOT NULL REFERENCES empleado(id),
    fecha_cambio    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS token_recuperacion (
    id              SERIAL PRIMARY KEY,
    empleado_id     INT NOT NULL REFERENCES empleado(id),
    token_hash      VARCHAR(64) NOT NULL UNIQUE,
    expira_en       TIMESTAMP NOT NULL,
    usado_en        TIMESTAMP,
    creado_en       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_historial_empleado ON historial_estado_empleado(empleado_id);
CREATE INDEX IF NOT EXISTS idx_token_recuperacion_empleado ON token_recuperacion(empleado_id);

-- Solo cambia las columnas que hoy están en BIGINT (si ya son INTEGER, no hace nada)
DO $$
DECLARE
    c RECORD;
BEGIN
    FOR c IN
        SELECT table_name, column_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND data_type = 'bigint'
          AND (table_name, column_name) IN (
              ('restaurante', 'id'), ('rol', 'id'),
              ('empleado', 'id'), ('empleado', 'restaurante_id'), ('empleado', 'rol_id'),
              ('ingrediente', 'id'), ('ingrediente', 'restaurante_id'),
              ('historial_estado_empleado', 'id'), ('historial_estado_empleado', 'empleado_id'),
              ('historial_estado_empleado', 'cambiado_por'),
              ('token_recuperacion', 'id'), ('token_recuperacion', 'empleado_id'))
    LOOP
        EXECUTE format('ALTER TABLE %I ALTER COLUMN %I TYPE INTEGER', c.table_name, c.column_name);
        RAISE NOTICE 'Columna %.% → INTEGER', c.table_name, c.column_name;
    END LOOP;
END $$;

COMMIT;
