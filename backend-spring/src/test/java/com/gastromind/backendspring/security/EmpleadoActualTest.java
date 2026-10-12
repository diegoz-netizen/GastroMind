package com.gastromind.backendspring.security;

import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.entity.Restaurante;
import com.gastromind.backendspring.entity.Rol;
import com.gastromind.backendspring.repository.EmpleadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoActualTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private EmpleadoActual empleadoActual;

    private Empleado admin;
    private Empleado mozo;

    @BeforeEach
    void setUp() {
        Restaurante restaurante = Restaurante.builder().id(7L).nombre("Restaurante Siete").build();
        admin = Empleado.builder().id(1L).restaurante(restaurante)
                .rol(Rol.builder().id(1L).nombre("ADMINISTRADOR").build())
                .nombreCompleto("Admin").correo("admin@resto7.com").passwordHash("hash").estado("ACTIVO").build();
        mozo = Empleado.builder().id(2L).restaurante(restaurante)
                .rol(Rol.builder().id(4L).nombre("MOZO").build())
                .nombreCompleto("Mozo").correo("mozo@resto7.com").passwordHash("hash").estado("ACTIVO").build();
    }

    private Authentication autenticado(Object principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    private void assertStatus(HttpStatus esperado, Runnable accion) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, accion::run);
        assertEquals(esperado, ex.getStatusCode());
    }

    @Test
    void obtener_EmpleadoActivo_DeberiaRetornarlo() {
        when(empleadoRepository.findById(2L)).thenReturn(Optional.of(mozo));

        assertSame(mozo, empleadoActual.obtener(autenticado(2L)));
    }

    @Test
    void restauranteId_DeberiaSalirDelEmpleadoAutenticado() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertEquals(7L, empleadoActual.restauranteId(autenticado(1L)));
    }

    @Test
    void obtener_SinAutenticacion_DeberiaLanzar401() {
        assertStatus(HttpStatus.UNAUTHORIZED, () -> empleadoActual.obtener(null));
        verifyNoInteractions(empleadoRepository);
    }

    @Test
    void obtener_PrincipalEsCorreo_DeberiaLanzar401SinBuscarPorCorreo() {
        // El bug de HU-01: tratar el principal como correo. El principal debe ser el id (Long).
        assertStatus(HttpStatus.UNAUTHORIZED, () -> empleadoActual.obtener(autenticado("admin@resto7.com")));
        verifyNoInteractions(empleadoRepository);
    }

    @Test
    void obtener_EmpleadoInexistente_DeberiaLanzar401() {
        when(empleadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertStatus(HttpStatus.UNAUTHORIZED, () -> empleadoActual.obtener(autenticado(99L)));
    }

    @Test
    void obtener_EmpleadoInactivo_DeberiaLanzar401() {
        mozo.setEstado("INACTIVO");
        when(empleadoRepository.findById(2L)).thenReturn(Optional.of(mozo));

        assertStatus(HttpStatus.UNAUTHORIZED, () -> empleadoActual.obtener(autenticado(2L)));
    }

    @Test
    void obtenerAdministrador_ConAdmin_DeberiaRetornarlo() {
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertSame(admin, empleadoActual.obtenerAdministrador(autenticado(1L)));
    }

    @Test
    void obtenerAdministrador_ConMozo_DeberiaLanzar403() {
        when(empleadoRepository.findById(2L)).thenReturn(Optional.of(mozo));

        assertStatus(HttpStatus.FORBIDDEN, () -> empleadoActual.obtenerAdministrador(autenticado(2L)));
    }
}
