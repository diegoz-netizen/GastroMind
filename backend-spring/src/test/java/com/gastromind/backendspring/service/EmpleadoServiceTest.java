package com.gastromind.backendspring.service;

import com.gastromind.backendspring.dto.EmpleadoRegistroRequest;
import com.gastromind.backendspring.dto.EmpleadoResponse;
import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.entity.Restaurante;
import com.gastromind.backendspring.entity.Rol;
import com.gastromind.backendspring.repository.EmpleadoRepository;
import com.gastromind.backendspring.repository.RolRepository;
import com.gastromind.backendspring.service.impl.EmpleadoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmpleadoServiceImpl empleadoService;

    private Empleado adminMock;
    private Rol rolMock;
    private Restaurante restauranteMock;

    @BeforeEach
    void setUp() {
        restauranteMock = new Restaurante();
        restauranteMock.setId(1L);

        adminMock = new Empleado();
        adminMock.setId(1L);
        adminMock.setCorreo("admin@restaurante.com");
        adminMock.setRestaurante(restauranteMock);

        rolMock = new Rol();
        rolMock.setId(2L);
        rolMock.setNombre("COCINERO");
    }

    @Test
    void registrarEmpleado_Exito() {
        EmpleadoRegistroRequest request = new EmpleadoRegistroRequest(
                "Carlos Perez", "carlos@restaurante.com", "password123", 2L, null
        );

        when(empleadoRepository.findByCorreo("admin@restaurante.com")).thenReturn(Optional.of(adminMock));
        when(empleadoRepository.existsByCorreo("carlos@restaurante.com")).thenReturn(false);
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolMock));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(invocation -> {
            Empleado e = invocation.getArgument(0);
            e.setId(10L);
            return e;
        });

        EmpleadoResponse response = empleadoService.registrarEmpleado(request, "admin@restaurante.com");

        assertNotNull(response);
        assertEquals("Carlos Perez", response.nombreCompleto());
        assertEquals("ACTIVO", response.estado());
        assertEquals("COCINERO", response.nombreRol());
        verify(empleadoRepository, times(1)).save(any(Empleado.class));
    }

    @Test
    void registrarEmpleado_CorreoDuplicado_LanzaExcepcion() {
        EmpleadoRegistroRequest request = new EmpleadoRegistroRequest(
                "Carlos Perez", "duplicado@restaurante.com", "password123", 2L, null
        );

        when(empleadoRepository.findByCorreo("admin@restaurante.com")).thenReturn(Optional.of(adminMock));
        when(empleadoRepository.existsByCorreo("duplicado@restaurante.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                empleadoService.registrarEmpleado(request, "admin@restaurante.com")
        );
    }
}