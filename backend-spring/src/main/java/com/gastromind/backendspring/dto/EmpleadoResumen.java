package com.gastromind.backendspring.dto;

import com.gastromind.backendspring.entity.Empleado;
import lombok.*;

/**
 * Datos visibles de un empleado. Nunca incluye password_hash ni pin_acceso.
 */
@Getter
@AllArgsConstructor
@Builder
public class EmpleadoResumen {
    private Long id;
    private String nombreCompleto;
    private String correo;
    private String rol;
    private String estado;

    public static EmpleadoResumen from(Empleado empleado) {
        return EmpleadoResumen.builder()
                .id(empleado.getId())
                .nombreCompleto(empleado.getNombreCompleto())
                .correo(empleado.getCorreo())
                .rol(empleado.getRol().getNombre())
                .estado(empleado.getEstado())
                .build();
    }
}
