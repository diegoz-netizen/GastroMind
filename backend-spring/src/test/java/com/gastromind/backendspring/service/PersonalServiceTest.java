package com.gastromind.backendspring.service;

import com.gastromind.backendspring.dto.EmpleadoResumen;
import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.entity.HistorialEstadoEmpleado;
import com.gastromind.backendspring.entity.Restaurante;
import com.gastromind.backendspring.entity.Rol;
import com.gastromind.backendspring.repository.HistorialEstadoEmpleadoRepository;
import com.gastromind.backendspring.repository.PersonalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class PersonalServiceTest {

    @Mock
    private PersonalRepository personalRepository;

    @Mock
    private HistorialEstadoEmpleadoRepository historialRepository;

    @InjectMocks
    private PersonalService personalService;

    private Restaurante restaurante;
    private Empleado admin;
    private Empleado mozo;

    @BeforeEach
    void setUp() {
        restaurante = Restaurante.builder().id(1L).nombre("Restaurante Uno").build();
        Rol rolAdmin = Rol.builder().id(1L).nombre("ADMINISTRADOR").build();
        Rol rolMozo = Rol.builder().id(4L).nombre("MOZO").build();

        admin = Empleado.builder().id(1L).restaurante(restaurante).rol(rolAdmin)
                .nombreCompleto("Admin Uno").correo("admin@resto1.com")
                .passwordHash("hash-admin").estado("ACTIVO").build();
        mozo = Empleado.builder().id(2L).restaurante(restaurante).rol(rolMozo)
                .nombreCompleto("Mozo Uno").correo("mozo@resto1.com")
                .passwordHash("hash-mozo").pinAcceso("hash-pin").estado("ACTIVO").build();
    }

    private Authentication autenticado(String correo) {
        return new UsernamePasswordAuthenticationToken(correo, null, List.of());
    }

    @Test
    void listar_devuelveSoloEmpleadosDelRestauranteDelAdmin() {
        when(personalRepository.findByCorreo("admin@resto1.com")).thenReturn(Optional.of(admin));
        when(personalRepository.findByRestauranteIdOrderByIdAsc(1L)).thenReturn(List.of(admin, mozo));

        List<EmpleadoResumen> resultado = personalService.listar(autenticado("admin@resto1.com"));

        assertEquals(2, resultado.size());
        assertEquals("mozo@resto1.com", resultado.get(1).getCorreo());
        verify(personalRepository).findByRestauranteIdOrderByIdAsc(1L);
    }

    @Test
    void cambiarEstado_desactivaSinBorrarYRegistraHistorial() {
        when(personalRepository.findByCorreo("admin@resto1.com")).thenReturn(Optional.of(admin));
        when(personalRepository.findByIdAndRestauranteId(2L, 1L)).thenReturn(Optional.of(mozo));

        EmpleadoResumen resultado = personalService.cambiarEstado(2L, "INACTIVO", autenticado("admin@resto1.com"));

        assertEquals("INACTIVO", resultado.getEstado());
        assertEquals("INACTIVO", mozo.getEstado());
        verify(personalRepository, never()).delete(any());

        ArgumentCaptor<HistorialEstadoEmpleado> captor = ArgumentCaptor.forClass(HistorialEstadoEmpleado.class);
        verify(historialRepository).save(captor.capture());
        assertEquals("ACTIVO", captor.getValue().getEstadoAnterior());
        assertEquals("INACTIVO", captor.getValue().getEstadoNuevo());
        assertSame(admin, captor.getValue().getCambiadoPor());
    }

    @Test
    void cambiarEstado_mismoEstadoNoRegistraHistorial() {
        when(personalRepository.findByCorreo("admin@resto1.com")).thenReturn(Optional.of(admin));
        when(personalRepository.findByIdAndRestauranteId(2L, 1L)).thenReturn(Optional.of(mozo));

        personalService.cambiarEstado(2L, "ACTIVO", autenticado("admin@resto1.com"));

        verify(historialRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_empleadoDeOtroRestauranteDa404() {
        when(personalRepository.findByCorreo("admin@resto1.com")).thenReturn(Optional.of(admin));
        when(personalRepository.findByIdAndRestauranteId(99L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> personalService.cambiarEstado(99L, "INACTIVO", autenticado("admin@resto1.com")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void cambiarEstado_estadoInvalidoDa400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> personalService.cambiarEstado(2L, "BORRADO", autenticado("admin@resto1.com")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(personalRepository);
    }

    @Test
    void noAdministradorDa403() {
        when(personalRepository.findByCorreo("mozo@resto1.com")).thenReturn(Optional.of(mozo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> personalService.listar(autenticado("mozo@resto1.com")));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void adminInactivoDa401() {
        admin.setEstado("INACTIVO");
        when(personalRepository.findByCorreo("admin@resto1.com")).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> personalService.listar(autenticado("admin@resto1.com")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void sinAutenticacionDa401() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> personalService.listar(null));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
