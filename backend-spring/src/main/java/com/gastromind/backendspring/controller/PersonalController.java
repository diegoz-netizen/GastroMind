package com.gastromind.backendspring.controller;

import com.gastromind.backendspring.dto.CambioEstadoRequest;
import com.gastromind.backendspring.dto.EmpleadoResumen;
import com.gastromind.backendspring.service.PersonalService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HU-03: GET y PATCH de personal. El POST de registro (HU-01) puede convivir en /api/empleados
 * siempre que no duplique estos mapeos.
 */
@RestController
@RequestMapping("/api/empleados")
public class PersonalController {

    private final PersonalService personalService;

    public PersonalController(PersonalService personalService) {
        this.personalService = personalService;
    }

    @GetMapping
    public List<EmpleadoResumen> listar(Authentication authentication) {
        return personalService.listar(authentication);
    }

    @PatchMapping("/{id}/estado")
    public EmpleadoResumen cambiarEstado(@PathVariable Long id,
                                         @RequestBody CambioEstadoRequest request,
                                         Authentication authentication) {
        return personalService.cambiarEstado(id, request.getEstado(), authentication);
    }
}
