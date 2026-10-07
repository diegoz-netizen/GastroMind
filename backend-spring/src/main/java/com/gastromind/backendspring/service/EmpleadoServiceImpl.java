package com.gastromind.backendspring.service.impl;

import com.gastromind.backendspring.dto.EmpleadoRegistroRequest;
import com.gastromind.backendspring.dto.EmpleadoResponse;
import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.entity.Rol;
import com.gastromind.backendspring.repository.EmpleadoRepository;
import com.gastromind.backendspring.repository.RolRepository;
import com.gastromind.backendspring.service.EmpleadoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository,
                               RolRepository rolRepository,
                               PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public EmpleadoResponse registrarEmpleado(EmpleadoRegistroRequest request, String correoAdminAutenticado) {
        // 1. Obtener al administrador logueado para extraer su restaurante_id (Multi-tenant)
        Empleado admin = empleadoRepository.findByCorreo(correoAdminAutenticado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        // 2. Validar correo no duplicado (409 Conflict)
        if (empleadoRepository.existsByCorreo(request.correo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado");
        }

        // 3. Validar existencia del rol (400 Bad Request)
        Rol rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol especificado no existe"));

        // 4. Crear nueva entidad Empleado
        Empleado nuevoEmpleado = new Empleado();
        nuevoEmpleado.setRestaurante(admin.getRestaurante());
        nuevoEmpleado.setRol(rol);
        nuevoEmpleado.setNombreCompleto(request.nombreCompleto());
        nuevoEmpleado.setCorreo(request.correo());

        // Encriptar contraseña con BCrypt
        nuevoEmpleado.setPasswordHash(passwordEncoder.encode(request.password()));

        if (request.pinAcceso() != null && !request.pinAcceso().isBlank()) {
            nuevoEmpleado.setPinAcceso(passwordEncoder.encode(request.pinAcceso()));
        }

        nuevoEmpleado.setEstado("ACTIVO");
        nuevoEmpleado.setFechaRegistro(LocalDateTime.now());

        Empleado guardado = empleadoRepository.save(nuevoEmpleado);

        // 5. Retornar DTO de respuesta (nunca retornar hashes)
        return new EmpleadoResponse(
                guardado.getId(),
                guardado.getRestaurante().getId(),
                guardado.getRol().getId(),
                guardado.getRol().getNombre(),
                guardado.getNombreCompleto(),
                guardado.getCorreo(),
                guardado.getEstado(),
                guardado.getFechaRegistro()
        );
    }
}