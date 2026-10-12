package com.gastromind.backendspring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita @Async: los correos (CorreoService) se envían en segundo plano con el pool de tareas de Spring Boot,
 * así la petición responde sin esperar al servidor SMTP.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
