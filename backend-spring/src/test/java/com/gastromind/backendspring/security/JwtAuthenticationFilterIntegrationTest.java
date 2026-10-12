package com.gastromind.backendspring.security;

import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.entity.Restaurante;
import com.gastromind.backendspring.entity.Rol;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-02 + HU-03, de punta a punta: un empleado que ya no está ACTIVO recibe 401 aunque presente
 * un token válido emitido antes de su desactivación. Levanta la aplicación real con H2 (perfil test).
 * Ojo: en /api/empleados el servicio de HU-03 también revisa el estado; el comportamiento del filtro
 * por sí solo lo prueba JwtAuthenticationFilterTest.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationFilterIntegrationTest {

    private static final String RUTA_PROTEGIDA = "/api/empleados";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Empleado admin;

    @BeforeEach
    void setUp() {
        Restaurante restaurante = Restaurante.builder().nombre("Restaurante Test").build();
        Rol rolAdmin = Rol.builder().nombre("ADMINISTRADOR").build();
        entityManager.persist(restaurante);
        entityManager.persist(rolAdmin);

        admin = Empleado.builder().restaurante(restaurante).rol(rolAdmin)
                .nombreCompleto("Admin Test").correo("admin@test.com")
                .passwordHash("hash").estado("ACTIVO").build();
        entityManager.persist(admin);
        entityManager.flush();
    }

    private String bearer(Empleado empleado) {
        return "Bearer " + jwtTokenProvider.generarToken(empleado);
    }

    @Test
    void empleadoActivo_ConTokenValido_DeberiaAcceder() throws Exception {
        mockMvc.perform(get(RUTA_PROTEGIDA).header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void empleadoDesactivadoDespuesDeEmitirSuToken_DeberiaRecibir401() throws Exception {
        String tokenEmitidoMientrasEstabaActivo = bearer(admin);

        admin.setEstado("INACTIVO");
        entityManager.flush();

        mockMvc.perform(get(RUTA_PROTEGIDA).header("Authorization", tokenEmitidoMientrasEstabaActivo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void empleadoQueYaNoExiste_ConTokenValido_DeberiaRecibir401() throws Exception {
        Empleado inexistente = Empleado.builder().id(999_999L)
                .restaurante(admin.getRestaurante()).rol(admin.getRol()).build();

        mockMvc.perform(get(RUTA_PROTEGIDA).header("Authorization", bearer(inexistente)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenConFirmaAlterada_DeberiaRecibir401() throws Exception {
        String token = bearer(admin);
        String alterado = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        mockMvc.perform(get(RUTA_PROTEGIDA).header("Authorization", alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinToken_DeberiaRecibir401() throws Exception {
        mockMvc.perform(get(RUTA_PROTEGIDA))
                .andExpect(status().isUnauthorized());
    }
}
