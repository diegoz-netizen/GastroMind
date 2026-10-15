-- ============================================================
-- GASTROMIND — Datos semilla de DEMO (HU-02 Login, HU-05 Insumos)
-- Ejecutar DESPUES de script_database.sql (los roles ya existen ahi,
-- por eso NO se reinsertan; se referencian por nombre).
--
-- Credenciales demo (solo entorno local/demo):
--   admin.central@gastromind.demo  /  Demo2026!
--   admin.norte@gastromind.demo    /  Demo2026!
-- Hash BCrypt (cost 10) generado con BCryptPasswordEncoder de Spring Security.
--
-- Regla del equipo: todo dato de demo, test o Postman va en este archivo,
-- nunca cargado a mano (si no, una BD creada desde el repo queda incompleta).
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

-- HU-05: insumos de la demo. Se buscan los restaurantes por nombre (los id
-- dependen del orden de carga) y se insertan en orden fijo. Tomate (Central) y Pescado (Norte) quedan
-- bajo su stock minimo a proposito: muestran la alerta "Bajo minimo".
INSERT INTO ingrediente (restaurante_id, nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario, tipo_control)
SELECT r.id, v.nombre, v.unidad, v.stock, v.minimo, v.costo, 'RECETA'
FROM (VALUES
    (1, 'GastroMind Demo - Sede Central', 'Aceite vegetal', 'l',  6.00,  4.00,  9.90),
    (2, 'GastroMind Demo - Sede Central', 'Pollo',          'kg', 12.00, 8.00,  11.50),
    (3, 'GastroMind Demo - Sede Central', 'Arroz',          'kg', 25.00, 10.00, 3.80),
    (4, 'GastroMind Demo - Sede Central', 'Tomate',         'kg', 2.00,  5.00,  4.50),
    (5, 'GastroMind Demo - Sede Norte',   'Limón',          'kg', 8.00,  3.00,  4.20),
    (6, 'GastroMind Demo - Sede Norte',   'Pescado',        'kg', 3.00,  5.00,  18.00)
) AS v(orden, restaurante, nombre, unidad, stock, minimo, costo)
JOIN restaurante r ON r.nombre = v.restaurante
ORDER BY v.orden;   -- mismo orden (y mismos id en una BD nueva) que la demo

COMMIT;
