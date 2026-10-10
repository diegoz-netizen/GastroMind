-- ============================================================
-- GASTROMIND — Datos semilla de DEMO (HU-02 Login)
-- Ejecutar DESPUES de script_database.sql (los roles ya existen ahi,
-- por eso NO se reinsertan; se referencian por nombre).
--
-- Credenciales demo (solo entorno local/demo):
--   admin.central@gastromind.demo  /  Demo2026!
--   admin.norte@gastromind.demo    /  Demo2026!
-- Hash BCrypt (cost 10) generado con BCryptPasswordEncoder de Spring Security.
-- ============================================================

BEGIN;

WITH r1 AS (
    INSERT INTO restaurante (nombre) VALUES ('GastroMind Demo - Sede Central') RETURNING id
), r2 AS (
    INSERT INTO restaurante (nombre) VALUES ('GastroMind Demo - Sede Norte') RETURNING id
)
INSERT INTO empleado (restaurante_id, rol_id, nombre_completo, correo, password_hash, estado)
SELECT r1.id, (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR'),
       'Administrador Sede Central', 'admin.central@gastromind.demo',
       '$2a$10$cahPqSeDX3.MH6ROtNPAc.rFurkPmsdPwS1wnxF94RZIrb/F5inba', 'ACTIVO'
FROM r1
UNION ALL
SELECT r2.id, (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR'),
       'Administrador Sede Norte', 'admin.norte@gastromind.demo',
       '$2a$10$cahPqSeDX3.MH6ROtNPAc.rFurkPmsdPwS1wnxF94RZIrb/F5inba', 'ACTIVO'
FROM r2;

COMMIT;
