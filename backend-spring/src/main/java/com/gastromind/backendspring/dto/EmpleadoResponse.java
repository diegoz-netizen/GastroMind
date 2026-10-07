package com.gastromind.backendspring.dto;

import java.time.LocalDateTime;

public record EmpleadoResponse(
        Long id,
        Long restauranteId,
        Long rolId,
        String nombreRol,
        String nombreCompleto,
        String correo,
        String estado,
        LocalDateTime fechaRegistro
) {}