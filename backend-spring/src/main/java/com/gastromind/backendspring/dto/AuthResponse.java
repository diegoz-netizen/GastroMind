package com.gastromind.backendspring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tipoToken = "Bearer";
    private Long empleadoId;
    private String nombreCompleto;
    private String correo;
    private String rol;
    private Long restauranteId;
}
