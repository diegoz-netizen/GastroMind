package com.gastromind.backendspring.service;

import com.gastromind.backendspring.config.RecuperacionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Envío de correos de texto plano por SMTP (Mailtrap en desarrollo; Brevo/SendGrid al final).
 * Común para el equipo: HU-04 (recuperar contraseña) y el correo de bienvenida de HU-01.
 * Las credenciales SMTP van por variables de entorno, nunca en el repo.
 * El envío es asíncrono (@Async, ver AsyncConfig): quien llama no espera al SMTP ni recibe sus errores.
 */
@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final JavaMailSender mailSender;
    private final RecuperacionProperties recuperacionProperties;

    public CorreoService(JavaMailSender mailSender, RecuperacionProperties recuperacionProperties) {
        this.mailSender = mailSender;
        this.recuperacionProperties = recuperacionProperties;
    }

    @Async
    public void enviar(String para, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(recuperacionProperties.getRemitente());
        mensaje.setTo(para);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
            // Corre en otro hilo: el error ya no llega a quien llamó, así que se registra aquí
            log.error("No se pudo enviar el correo \"{}\"", asunto, e);
        }
    }
}
