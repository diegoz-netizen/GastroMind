package com.gastromind.backendspring.service;

import com.gastromind.backendspring.dto.EmpleadoResumen;
import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.entity.EstadoEmpleado;
import com.gastromind.backendspring.entity.HistorialEstadoEmpleado;
import com.gastromind.backendspring.repository.HistorialEstadoEmpleadoRepository;
import com.gastromind.backendspring.repository.PersonalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * HU-03: listar el personal del restaurante y activar/desactivar empleados.
 * Nunca se borra un empleado (borrado lógico): solo cambia su estado.
 */
@Service
public class PersonalService {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final PersonalRepository personalRepository;
    private final HistorialEstadoEmpleadoRepository historialRepository;

    public PersonalService(PersonalRepository personalRepository,
                           HistorialEstadoEmpleadoRepository historialRepository) {
        this.personalRepository = personalRepository;
        this.historialRepository = historialRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpleadoResumen> listar(Authentication authentication) {
        Empleado administrador = administradorActual(authentication);
        return personalRepository.findByRestauranteIdOrderByIdAsc(administrador.getRestaurante().getId())
                .stream()
                .map(EmpleadoResumen::from)
                .toList();
    }

    @Transactional
    public EmpleadoResumen cambiarEstado(Long id, String estado, Authentication authentication) {
        EstadoEmpleado nuevoEstado = validarEstado(estado);
        Empleado administrador = administradorActual(authentication);

        // Un empleado de otro restaurante se trata como inexistente (no se revela que existe)
        Empleado empleado = personalRepository.findByIdAndRestauranteId(id, administrador.getRestaurante().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));

        String estadoAnterior = empleado.getEstado();
        if (nuevoEstado.name().equals(estadoAnterior)) {
            return EmpleadoResumen.from(empleado);
        }

        empleado.setEstado(nuevoEstado.name());
        historialRepository.save(HistorialEstadoEmpleado.builder()
                .empleado(empleado)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(nuevoEstado.name())
                .cambiadoPor(administrador)
                .build());

        return EmpleadoResumen.from(empleado);
    }

    private EstadoEmpleado validarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }
        try {
            return EstadoEmpleado.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no válido: use ACTIVO o INACTIVO");
        }
    }

    /**
     * El principal autenticado es el id del empleado (lo pone el JwtAuthenticationFilter de HU-02).
     * Se consulta la BD en cada petición para exigir que siga ACTIVO y sea ADMINISTRADOR.
     */
    private Empleado administradorActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long empleadoId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticación requerida");
        }
        Empleado actual = personalRepository.findById(empleadoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identidad no válida"));
        if (!EstadoEmpleado.ACTIVO.name().equals(actual.getEstado())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Su cuenta se encuentra inactiva");
        }
        if (!ROL_ADMINISTRADOR.equalsIgnoreCase(actual.getRol().getNombre())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol ADMINISTRADOR");
        }
        return actual;
    }
}
