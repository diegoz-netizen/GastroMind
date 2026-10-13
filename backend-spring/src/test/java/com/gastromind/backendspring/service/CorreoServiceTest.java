package com.gastromind.backendspring.service;

import com.gastromind.backendspring.config.AsyncConfig;
import com.gastromind.backendspring.config.RecuperacionProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Solo AsyncConfig + CorreoService: comprueba que el proxy de @Async envía el correo en otro hilo
@SpringJUnitConfig(classes = {AsyncConfig.class, CorreoService.class})
class CorreoServiceTest {

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private RecuperacionProperties recuperacionProperties;

    @Autowired
    private CorreoService correoService;

    @Test
    void enviar_DeberiaEnviarseEnOtroHilo() throws Exception {
        CompletableFuture<SimpleMailMessage> enviado = new CompletableFuture<>();
        CompletableFuture<Thread> hilo = new CompletableFuture<>();
        doAnswer(inv -> {
            hilo.complete(Thread.currentThread());
            enviado.complete(inv.getArgument(0));
            return null;
        }).when(mailSender).send(any(SimpleMailMessage.class));
        when(recuperacionProperties.getRemitente()).thenReturn("no-responder@gastromind.demo");

        correoService.enviar("ana@test.com", "Asunto", "Cuerpo");

        assertNotEquals(Thread.currentThread(), hilo.get(5, TimeUnit.SECONDS));
        SimpleMailMessage mensaje = enviado.get(5, TimeUnit.SECONDS);
        assertArrayEquals(new String[]{"ana@test.com"}, mensaje.getTo());
        assertEquals("no-responder@gastromind.demo", mensaje.getFrom());
        assertEquals("Asunto", mensaje.getSubject());
    }

    @Test
    void enviar_FallaElSmtp_NoDeberiaPropagarElError() {
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(SimpleMailMessage.class));
        CorreoService sinProxy = new CorreoService(mailSender, recuperacionProperties);

        assertDoesNotThrow(() -> sinProxy.enviar("ana@test.com", "Asunto", "Cuerpo"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
