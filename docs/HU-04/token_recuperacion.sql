-- HU-04: enlaces de recuperación de contraseña (CA-04: caducan y son de un solo uso).
-- Tabla nueva; no modifica las tablas existentes de docs/script_database.sql.
-- Ejecutar después del script principal.

CREATE TABLE IF NOT EXISTS token_recuperacion (
    id           SERIAL PRIMARY KEY,
    empleado_id  INT NOT NULL REFERENCES empleado(id),
    token_hash   VARCHAR(64) NOT NULL UNIQUE,   -- SHA-256 del token; el token nunca se guarda
    expira_en    TIMESTAMP NOT NULL,
    usado_en     TIMESTAMP,                     -- NULL = sin usar; con fecha = ya no sirve
    creado_en    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Al pedir un enlace nuevo se anulan los pendientes del empleado
CREATE INDEX IF NOT EXISTS idx_token_recuperacion_empleado ON token_recuperacion(empleado_id);
