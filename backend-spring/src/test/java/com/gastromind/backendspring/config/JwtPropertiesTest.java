package com.gastromind.backendspring.config;

import com.gastromind.backendspring.entity.Empleado;
import com.gastromind.backendspring.entity.Restaurante;
import com.gastromind.backendspring.entity.Rol;
import com.gastromind.backendspring.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-02: configuración del JWT al arrancar.
 * Antes, sin gastromind.jwt.expiration-ms el token nacía vencido (exp == iat) y todo daba 401.
 */
class JwtPropertiesTest {

    private static final String CLAVE_32 = "0123456789abcdef0123456789abcdef";

    @Configuration
    @EnableConfigurationProperties
    @Import({JwtProperties.class, JwtTokenProvider.class})
    static class Config {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Config.class);

    @Test
    void sinExpirationMs_elTokenDura24hYEsValido() {
        runner.withPropertyValues("gastromind.jwt.secret=" + CLAVE_32).run(ctx -> {
            assertThat(ctx).hasNotFailed();
            JwtTokenProvider provider = ctx.getBean(JwtTokenProvider.class);

            String token = provider.generarToken(empleado());
            Claims claims = leer(token, CLAVE_32);

            assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                    .isEqualTo(86_400_000L);
            assertThat(provider.validateToken(token)).isTrue();
        });
    }

    @Test
    void respetaExpirationMsConfigurada() {
        runner.withPropertyValues("gastromind.jwt.secret=" + CLAVE_32, "gastromind.jwt.expiration-ms=3600000")
                .run(ctx -> {
                    Claims claims = leer(ctx.getBean(JwtTokenProvider.class).generarToken(empleado()), CLAVE_32);
                    assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                            .isEqualTo(3_600_000L);
                });
    }

    @Test
    void noArrancaSinClave() {
        runner.run(ctx -> assertFallaCon(ctx, "Falta gastromind.jwt.secret"));
    }

    @Test
    void noArrancaConClaveEnBlanco() {
        runner.withPropertyValues("gastromind.jwt.secret=   ")
                .run(ctx -> assertFallaCon(ctx, "Falta gastromind.jwt.secret"));
    }

    @Test
    void noArrancaConClaveDe31Bytes() {
        runner.withPropertyValues("gastromind.jwt.secret=" + CLAVE_32.substring(1))
                .run(ctx -> assertFallaCon(ctx, "mide 31 bytes"));
    }

    @Test
    void laLongitudSeMideEnBytesUtf8() {
        // 16 "ñ" = 16 caracteres pero 32 bytes en UTF-8 → válida
        runner.withPropertyValues("gastromind.jwt.secret=" + "ñ".repeat(16))
                .run(ctx -> assertThat(ctx).hasNotFailed());
        // 15 "ñ" + "a" = 31 bytes → inválida
        runner.withPropertyValues("gastromind.jwt.secret=" + "ñ".repeat(15) + "a")
                .run(ctx -> assertFallaCon(ctx, "mide 31 bytes"));
    }

    @Test
    void firmaConLosBytesUtf8DeLaClave_comoDjango() {
        String clave = "clave-compartida-con-django-ñandú";
        runner.withPropertyValues("gastromind.jwt.secret=" + clave).run(ctx -> {
            String token = ctx.getBean(JwtTokenProvider.class).generarToken(empleado());
            // Django (PyJWT) arma la clave con clave.encode('utf-8')
            assertThat(leer(token, clave).get("usuario_id", Long.class)).isEqualTo(1L);
        });
    }

    @Test
    void noArrancaConExpirationCeroONegativa() {
        runner.withPropertyValues("gastromind.jwt.secret=" + CLAVE_32, "gastromind.jwt.expiration-ms=0")
                .run(ctx -> assertFallaCon(ctx, "expiration-ms debe ser mayor que 0"));
        runner.withPropertyValues("gastromind.jwt.secret=" + CLAVE_32, "gastromind.jwt.expiration-ms=-1")
                .run(ctx -> assertFallaCon(ctx, "expiration-ms debe ser mayor que 0"));
    }

    private static void assertFallaCon(AssertableApplicationContext ctx, String mensaje) {
        assertThat(ctx).hasFailed();
        assertThat(ctx.getStartupFailure()).rootCause().hasMessageContaining(mensaje);
    }

    private static Claims leer(String token, String clave) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(clave.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static Empleado empleado() {
        return Empleado.builder()
                .id(1L)
                .correo("admin@test.com")
                .rol(Rol.builder().id(1L).nombre("ADMINISTRADOR").build())
                .restaurante(Restaurante.builder().id(1L).nombre("Central").build())
                .estado("ACTIVO")
                .build();
    }
}
