package com.gastromind.backendspring.security;

import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.repository.EmpleadoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * El filtro por sí solo (sin los servicios de cada HU): solo autentica a empleados que siguen ACTIVO en la BD.
 * Si no autentica, la petición sigue y el entry point de SecurityConfig responde 401.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "token-de-prueba";

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private JwtAuthenticationFilter filtro;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private Authentication ejecutarFiltro(String authorization) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/empleados");
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        MockFilterChain chain = new MockFilterChain();
        filtro.doFilter(request, new MockHttpServletResponse(), chain);

        assertNotNull(chain.getRequest(), "la petición siempre debe continuar por la cadena de filtros");
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private void tokenValidoDe(Long empleadoId) {
        when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getEmpleadoIdFromToken(TOKEN)).thenReturn(empleadoId);
        when(jwtTokenProvider.getRestauranteIdFromToken(TOKEN)).thenReturn(1L);
    }

    private Empleado empleadoConEstado(String estado) {
        return Empleado.builder().id(2L).estado(estado).build();
    }

    @Test
    void empleadoActivo_DeberiaQuedarAutenticadoConSuIdComoPrincipal() throws Exception {
        tokenValidoDe(2L);
        when(empleadoRepository.findById(2L)).thenReturn(Optional.of(empleadoConEstado("ACTIVO")));

        Authentication auth = ejecutarFiltro("Bearer " + TOKEN);

        assertNotNull(auth);
        assertEquals(2L, auth.getPrincipal());
    }

    @Test
    void empleadoInactivo_ConTokenValido_NoDeberiaQuedarAutenticado() throws Exception {
        tokenValidoDe(2L);
        when(empleadoRepository.findById(2L)).thenReturn(Optional.of(empleadoConEstado("INACTIVO")));

        assertNull(ejecutarFiltro("Bearer " + TOKEN));
    }

    @Test
    void empleadoInexistente_ConTokenValido_NoDeberiaQuedarAutenticado() throws Exception {
        tokenValidoDe(2L);
        when(empleadoRepository.findById(2L)).thenReturn(Optional.empty());

        assertNull(ejecutarFiltro("Bearer " + TOKEN));
    }

    @Test
    void tokenInvalido_NoDeberiaAutenticarNiConsultarLaBd() throws Exception {
        when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(false);

        assertNull(ejecutarFiltro("Bearer " + TOKEN));
        verifyNoInteractions(empleadoRepository);
    }

    @Test
    void sinCabeceraAuthorization_NoDeberiaAutenticar() throws Exception {
        assertNull(ejecutarFiltro(null));
        verifyNoInteractions(jwtTokenProvider, empleadoRepository);
    }
}
