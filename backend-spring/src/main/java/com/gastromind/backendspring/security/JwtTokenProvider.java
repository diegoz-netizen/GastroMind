package com.gastromind.backendspring.security;

import com.gastromind.backendspring.config.JwtProperties;
import com.gastromind.backendspring.entity.Empleado;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Autowired
    private JwtProperties jwtProperties;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generarToken(Empleado empleado) {
        Date ahora = new Date();
        Date fechaExpiracion = new Date(ahora.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .setSubject(empleado.getCorreo())
                .claim("usuario_id", empleado.getId())
                .claim("rol_id", empleado.getRol().getId())
                .claim("restaurante_id", empleado.getRestaurante().getId())
                .setIssuedAt(ahora)
                .setExpiration(fechaExpiracion)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
