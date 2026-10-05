package com.gastromind.backendspring.entity;

/**
 * Estados permitidos para un empleado (CHECK de la tabla empleado).
 * La entidad Empleado guarda el valor como texto: usar {@link #name()}.
 */
public enum EstadoEmpleado {
    ACTIVO,
    INACTIVO
}
