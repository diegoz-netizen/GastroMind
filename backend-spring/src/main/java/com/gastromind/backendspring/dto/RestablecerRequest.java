package com.gastromind.backendspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerRequest(
        @NotBlank(message = "El enlace no es válido")
        String token,

        // Misma regla que el registro de HU-01
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String nuevaContrasena
) {}
