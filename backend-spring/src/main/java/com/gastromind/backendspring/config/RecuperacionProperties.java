package com.gastromind.backendspring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * HU-04: configuración del enlace de recuperación.
 * expiracionMinutos: 15 (CA-04, decidido el 12/10). Se cambia solo en properties.
 */
@Configuration
@ConfigurationProperties(prefix = "gastromind.recuperacion")
public class RecuperacionProperties {

    private long expiracionMinutos = 15;
    private String urlRestablecer = "http://localhost:5173/restablecer";
    private String remitente = "no-responder@gastromind.demo";

    public long getExpiracionMinutos() {
        return expiracionMinutos;
    }

    public void setExpiracionMinutos(long expiracionMinutos) {
        this.expiracionMinutos = expiracionMinutos;
    }

    public String getUrlRestablecer() {
        return urlRestablecer;
    }

    public void setUrlRestablecer(String urlRestablecer) {
        this.urlRestablecer = urlRestablecer;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }
}
