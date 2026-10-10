package com.gastromind.backendspring.dto;

public record EmpleadoResponse(
        Long id,
        String nombreCompleto,
        String correo,
        String rol,
        Long restauranteId,
        String estado
) {}