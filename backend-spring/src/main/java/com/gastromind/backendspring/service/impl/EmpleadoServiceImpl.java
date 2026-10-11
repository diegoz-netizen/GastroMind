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
    public EmpleadoResponse registrarEmpleado(EmpleadoRegistroRequest request, String adminIdStr) {
        Long adminId;
        try {
            adminId = Long.valueOf(adminIdStr);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        // 1. Buscar al admin autenticado por ID
        Empleado admin = empleadoRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        // 2. Validar que el admin este ACTIVO y tenga rol ADMINISTRADOR
        if (!"ACTIVO".equalsIgnoreCase(admin.getEstado())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Su cuenta se encuentra inactiva");
        }
        if (!"ADMINISTRADOR".equalsIgnoreCase(admin.getRol().getNombre())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol ADMINISTRADOR");
        }

        // 3. Validar correo duplicado (409)
        if (empleadoRepository.existsByCorreo(request.correo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya esta registrado");
        }

        // 4. Buscar rol a asignar
        Rol rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no valido"));

        // 5. Crear el nuevo empleado asignando el restaurante del admin
        Empleado nuevo = new Empleado();
        nuevo.setNombreCompleto(request.nombreCompleto());
        nuevo.setCorreo(request.correo());
        nuevo.setPasswordHash(passwordEncoder.encode(request.password())); // Usamos setPasswordHash
        nuevo.setPinAcceso(request.pinAcceso());
        nuevo.setRol(rol);
        nuevo.setRestaurante(admin.getRestaurante());
        nuevo.setEstado("ACTIVO"); // Usamos setEstado("ACTIVO")

        Empleado guardado = empleadoRepository.save(nuevo);

        return new EmpleadoResponse(
                guardado.getId(),
                guardado.getNombreCompleto(),
                guardado.getCorreo(),
                guardado.getRol().getNombre(),
                guardado.getRestaurante().getId(),
                guardado.getEstado()
        );
    }
}