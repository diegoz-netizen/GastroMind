# HU-03 — Activar y desactivar empleados

**Sprint:** 1 (semanas 7-8) · **Épica:** EP-01 Acceso y seguridad · **Responsable:** Victor Santamaría
**Rama:** `feature/HU-03-estado-empleados` (creada desde `feature/HU-02-login`) · **Actualizado:** 05/10/2026

## Historia de usuario

> Yo como gerente debo poder activar o desactivar usuarios para retirar el acceso al personal que ya no labora.

**Criterio de aceptación (CA-03):** un usuario desactivado no puede iniciar sesión y el sistema le informa que su cuenta está inactiva.

Reglas acordadas por el equipo:

- Solo un `ADMINISTRADOR` activo puede listar y cambiar estados.
- Solo ve y modifica empleados de **su** restaurante; el `restaurante_id` se obtiene del usuario autenticado, nunca del cliente.
- Estados válidos: `ACTIVO` e `INACTIVO`.
- El empleado **nunca se borra** (borrado lógico): pedidos, mermas e ingresos lo referencian.
- La respuesta nunca incluye `password_hash` ni `pin_acceso`.

## Tareas del Sprint Backlog

| Tarea | Estimado | Estado |
|---|---|---|
| Agregar el campo estado (activo/inactivo) al modelo de usuario | 2 h | Hecho: `Empleado.estado` (HU-02) + enum `EstadoEmpleado` para validar |
| Crear la vista de listado de empleados con opción de activar/desactivar | 3 h | Backend listo (`GET` y `PATCH`). Vista pendiente hasta definir el cliente |
| Implementar la lógica para bloquear el acceso de usuarios inactivos | 2 h | Login: ya lo rechaza HU-02. Endpoints de HU-03: revalidan el estado en BD en cada petición |
| Registrar el historial de cambios de estado de cada usuario | 2 h | Hecho: tabla `historial_estado_empleado` |

## Endpoints

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/empleados` | ADMINISTRADOR | Lista el personal del restaurante del admin |
| PATCH | `/api/empleados/{id}/estado` | ADMINISTRADOR | Cambia el estado y registra el historial |

Ejemplo:

```http
PATCH /api/empleados/2/estado
Authorization: Bearer <token>
Content-Type: application/json

{ "estado": "INACTIVO" }
```

```json
{ "id": 2, "nombreCompleto": "Mozo Uno", "correo": "mozo@resto1.com", "rol": "MOZO", "estado": "INACTIVO" }
```

| Código | Cuándo |
|---|---|
| 200 | Cambio realizado (si ya tenía ese estado, responde igual y no registra historial) |
| 400 | Estado vacío o distinto de `ACTIVO` / `INACTIVO` |
| 401 | Sin autenticación o el admin está inactivo |
| 403 | El usuario no es `ADMINISTRADOR` |
| 404 | El empleado no existe **en su restaurante** (no se revela si existe en otro) |

## Archivos

Paquete `com.gastromind.backendspring`. Todos son archivos nuevos; no se modificó ningún archivo de HU-02.

```text
entity/      EstadoEmpleado, HistorialEstadoEmpleado
repository/  PersonalRepository, HistorialEstadoEmpleadoRepository
dto/         CambioEstadoRequest, EmpleadoResumen
service/     PersonalService
controller/  PersonalController
test/        service/PersonalServiceTest
docs/HU-03/  README.md, historial_estado_empleado.sql
```

- Se usan las entidades compartidas `Empleado`, `Rol` y `Restaurante` de HU-02 tal como están.
- `PersonalRepository` es un repositorio aparte para no tocar `EmpleadoRepository`.

## Base de datos

Script: [`historial_estado_empleado.sql`](./historial_estado_empleado.sql). Crea la tabla del historial y un índice en `empleado(restaurante_id)` para el listado. Se ejecuta después de `docs/script_database.sql`.

| Columna | Descripción |
|---|---|
| `empleado_id` | Empleado al que se le cambió el estado |
| `estado_anterior` / `estado_nuevo` | `ACTIVO` o `INACTIVO` |
| `cambiado_por` | Administrador que hizo el cambio |
| `fecha_cambio` | Fecha y hora del cambio |

## Integración con HU-01 y HU-02

- **Identidad:** `PersonalService` toma `Authentication.getName()` como el **correo**, que coincide con el `subject` del JWT de HU-02.
- **Filtro JWT:** estos endpoints responden cuando la petición llega autenticada. Necesitan el filtro que valide el token en cada petición (parte de HU-02).
- **Revocar tokens vigentes:** para que un empleado desactivado pierda acceso a **todos** los endpoints con un token anterior, el filtro de HU-02 debe consultar el estado actual en cada petición. HU-03 ya lo hace en sus propios endpoints.
- **HU-01:** el `POST /api/empleados` puede ir en otro controlador sobre la misma ruta, siempre que no repita los mapeos `GET` y `PATCH`.

## Pruebas

```bash
cd backend-spring
./mvnw test
```

`PersonalServiceTest` (JUnit 5 + Mockito, sin base de datos) cubre:

- Listado solo del restaurante del admin.
- Desactivar sin borrar y registrar el historial.
- Mismo estado: no registra historial.
- Empleado de otro restaurante → 404.
- Estado inválido → 400.
- Usuario sin rol admin → 403.
- Admin inactivo o sin autenticación → 401.

Resultado al 05/10/2026: 14 pruebas, 0 fallos (8 de HU-03 + 6 de HU-02).

## Pendiente (decisiones del equipo y del PO)

- ¿Un administrador puede desactivarse a sí mismo?
- ¿Se puede desactivar al último administrador activo del restaurante?
- Cliente para la demo (React o Kotlin) para construir la vista con el switch de estado.
- Quién mantiene el esquema compartido (script SQL o migraciones).
