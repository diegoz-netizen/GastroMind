package com.gastromind.backendspring.controller;

import com.gastromind.backendspring.dto.EmpleadoRegistroRequest;
import com.gastromind.backendspring.dto.EmpleadoResponse;
import com.gastromind.backendspring.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "*") // <--- Agregar esta anotación
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EmpleadoResponse> registrarEmpleado(
            @Valid @RequestBody EmpleadoRegistroRequest request,
            Authentication authentication) {

        String adminIdStr = authentication.getName();
        EmpleadoResponse respuesta = empleadoService.registrarEmpleado(request, adminIdStr);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}