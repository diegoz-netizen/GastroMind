package com.gastromind.backendspring.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
@ConfigurationProperties(prefix = "gastromind.jwt")
public class JwtProperties implements InitializingBean {

    /** HS256 exige una clave de al menos 256 bits. */
    static final int MIN_SECRET_BYTES = 32;

    /** 24 h. Si falta la propiedad, el token ya no nace vencido (exp == iat). */
    static final long DEFAULT_EXPIRATION_MS = 86_400_000L;

    private String secret;
    private long expirationMs = DEFAULT_EXPIRATION_MS;

    /**
     * Se ejecuta al arrancar, después de leer las propiedades: si la configuración del JWT
     * es inválida, Spring no arranca y el mensaje dice qué falta (en vez de 401 sin explicación).
     */
    @Override
    public void afterPropertiesSet() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Falta gastromind.jwt.secret (ver application-local.properties.example)");
        }
        int bytes = getSecretBytes().length;
        if (bytes < MIN_SECRET_BYTES) {
            throw new IllegalStateException("gastromind.jwt.secret mide " + bytes
                    + " bytes; HS256 necesita al menos " + MIN_SECRET_BYTES);
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException(
                    "gastromind.jwt.expiration-ms debe ser mayor que 0 (recibido: " + expirationMs + ")");
        }
    }

    /** Siempre UTF-8, igual que Django (PyJWT), para que la misma clave valide en los dos backends. */
    public byte[] getSecretBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
