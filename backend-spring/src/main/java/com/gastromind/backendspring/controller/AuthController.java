package com.gastromind.backendspring.controller;

import com.gastromind.backendspring.dto.AuthResponse;
import com.gastromind.backendspring.dto.LoginRequest;
import com.gastromind.backendspring.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"}) // TODO: cambiar al dominio real en producción
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        // Credenciales inválidas o cuenta inactiva → 401 con {error, mensaje} (GlobalExceptionHandler, HL-014)
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}