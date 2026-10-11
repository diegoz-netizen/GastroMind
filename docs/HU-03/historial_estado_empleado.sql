-- HU-03: historial de cambios de estado de empleados (tarea del Sprint Backlog).
-- Tabla nueva; no modifica las tablas existentes de docs/script_database.sql.
-- Ejecutar después del script principal.

CREATE TABLE IF NOT EXISTS historial_estado_empleado (
    id               SERIAL PRIMARY KEY,
    empleado_id      INT NOT NULL REFERENCES empleado(id),
    estado_anterior  VARCHAR(20) NOT NULL CHECK (estado_anterior IN ('ACTIVO', 'INACTIVO')),
    estado_nuevo     VARCHAR(20) NOT NULL CHECK (estado_nuevo IN ('ACTIVO', 'INACTIVO')),
    cambiado_por     INT NOT NULL REFERENCES empleado(id),
    fecha_cambio     TIMESTAMP NOT NULL DEFAULT NOW()
);

-- El listado de HU-03 filtra empleados por restaurante
CREATE INDEX IF NOT EXISTS idx_empleado_restaurante ON empleado(restaurante_id);
CREATE INDEX IF NOT EXISTS idx_historial_estado_empleado ON historial_estado_empleado(empleado_id);
