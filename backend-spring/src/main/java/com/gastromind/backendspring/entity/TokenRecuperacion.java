package com.gastromind.backendspring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * HU-04: enlace de recuperación de contraseña.
 * Se guarda solo el hash SHA-256 del token (nunca el token): si alguien lee la BD, no puede usar los enlaces.
 * CA-04: caduca (expiraEn) y solo se usa una vez (usadoEn).
 */
@Entity
@Table(name = "token_recuperacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JdbcTypeCode(SqlTypes.INTEGER) // la columna es SERIAL (INT) en script_database.sql
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "usado_en")
    private LocalDateTime usadoEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        if (this.creadoEn == null) {
            this.creadoEn = LocalDateTime.now();
        }
    }

    public boolean estaVigente(LocalDateTime ahora) {
        return usadoEn == null && ahora.isBefore(expiraEn);
    }
}
