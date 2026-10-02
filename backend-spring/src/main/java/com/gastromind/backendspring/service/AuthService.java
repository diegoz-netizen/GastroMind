package com.gastromind.backendspring.service;

import com.gastromind.backendspring.dto.AuthResponse;
import com.gastromind.backendspring.dto.LoginRequest;
import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.repository.EmpleadoRepository;
import com.gastromind.backendspring.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public AuthResponse login(LoginRequest request) {
        // 1. Buscar empleado por correo
        Empleado empleado = empleadoRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!"ACTIVO".equalsIgnoreCase(empleado.getEstado())) {
            throw new RuntimeException("Su cuenta se encuentra inactiva. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(request.getPassword(), empleado.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = tokenProvider.generarToken(empleado);

        return AuthResponse.builder()
                .token(token)
                .empleadoId(empleado.getId())
                .nombreCompleto(empleado.getNombreCompleto())
                .correo(empleado.getCorreo())
                .rol(empleado.getRol().getNombre())
                .restauranteId(empleado.getRestaurante().getId())
                .build();
    }
}