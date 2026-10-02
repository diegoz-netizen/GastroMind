package com.gastromind.backendspring.service;

import com.gastromind.backendspring.config.JwtProperties;
import com.gastromind.backendspring.dto.AuthResponse;
import com.gastromind.backendspring.dto.LoginRequest;
import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.entity.Rol;
import com.gastromind.backendspring.entity.Restaurante;
import com.gastromind.backendspring.repository.EmpleadoRepository;
import com.gastromind.backendspring.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    private Empleado empleadoActivo;
    private Empleado empleadoInactivo;
    private Rol rol;
    private Restaurante restaurante;

    @BeforeEach
    void setUp() {
        restaurante = Restaurante.builder()
                .id(1L)
                .nombre("Restaurante Test")
                .build();

        rol = Rol.builder()
                .id(1L)
                .nombre("ADMINISTRADOR")
                .build();

        empleadoActivo = Empleado.builder()
                .id(1L)
                .nombreCompleto("Juan Pérez")
                .correo("juan@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .rol(rol)
                .restaurante(restaurante)
                .estado("ACTIVO")
                .build();

        empleadoInactivo = Empleado.builder()
                .id(2L)
                .nombreCompleto("María López")
                .correo("maria@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .rol(rol)
                .restaurante(restaurante)
                .estado("INACTIVO")
                .build();
    }

    @Test
    void login_CredencialesValidas_DeberiaRetornarAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setCorreo("juan@test.com");
        request.setPassword("password123");

        when(empleadoRepository.findByCorreo("juan@test.com"))
                .thenReturn(Optional.of(empleadoActivo));
        when(passwordEncoder.matches("password123", empleadoActivo.getPasswordHash()))
                .thenReturn(true);
        when(tokenProvider.generarToken(empleadoActivo))
                .thenReturn("mock-jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals(1L, response.getEmpleadoId());
        assertEquals("Juan Pérez", response.getNombreCompleto());
        assertEquals("juan@test.com", response.getCorreo());
        assertEquals("ADMINISTRADOR", response.getRol());
        assertEquals(1L, response.getRestauranteId());

        verify(empleadoRepository).findByCorreo("juan@test.com");
        verify(passwordEncoder).matches("password123", empleadoActivo.getPasswordHash());
        verify(tokenProvider).generarToken(empleadoActivo);
    }

    @Test
    void login_CredencialesInvalidas_NoExisteUsuario_DeberiaLanzarExcepcion() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setCorreo("nocoincide@test.com");
        request.setPassword("password123");

        when(empleadoRepository.findByCorreo("nocoincide@test.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(empleadoRepository).findByCorreo("nocoincide@test.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_ContraseniaIncorrecta_DeberiaLanzarExcepcion() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setCorreo("juan@test.com");
        request.setPassword("passwordIncorrecta");

        when(empleadoRepository.findByCorreo("juan@test.com"))
                .thenReturn(Optional.of(empleadoActivo));
        when(passwordEncoder.matches("passwordIncorrecta", empleadoActivo.getPasswordHash()))
                .thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(empleadoRepository).findByCorreo("juan@test.com");
        verify(passwordEncoder).matches("passwordIncorrecta", empleadoActivo.getPasswordHash());
        verify(tokenProvider, never()).generarToken(any());
    }

    @Test
    void login_UsuarioInactivo_DeberiaLanzarExcepcion() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setCorreo("maria@test.com");
        request.setPassword("password123");

        when(empleadoRepository.findByCorreo("maria@test.com"))
                .thenReturn(Optional.of(empleadoInactivo));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertTrue(exception.getMessage().contains("inactiva"));
        verify(empleadoRepository).findByCorreo("maria@test.com");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProvider, never()).generarToken(any());
    }

    @Test
    void login_UsuarioInactivo_NuncaValidaPassword_DeberiaRechazarAntes() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setCorreo("maria@test.com");
        request.setPassword("password123");

        when(empleadoRepository.findByCorreo("maria@test.com"))
                .thenReturn(Optional.of(empleadoInactivo));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Su cuenta se encuentra inactiva. Contacte al administrador.", exception.getMessage());

        // Verificar que NUNCA se verifica la contraseña del usuario inactivo
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenProvider, never()).generarToken(any());
    }
}
