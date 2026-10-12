package com.gastromind.backendspring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo es inválido")
        String correo
) {}
