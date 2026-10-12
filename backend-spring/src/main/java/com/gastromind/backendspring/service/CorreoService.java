package com.gastromind.backendspring.service;

import com.gastromind.backendspring.config.RecuperacionProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío de correos de texto plano por SMTP (Mailtrap en desarrollo; Brevo/SendGrid al final).
 * Común para el equipo: HU-04 (recuperar contraseña) y el correo de bienvenida de HU-01.
 * Las credenciales SMTP van por variables de entorno, nunca en el repo.
 */
@Service
public class CorreoService {

    private final JavaMailSender mailSender;
    private final RecuperacionProperties recuperacionProperties;

    public CorreoService(JavaMailSender mailSender, RecuperacionProperties recuperacionProperties) {
        this.mailSender = mailSender;
        this.recuperacionProperties = recuperacionProperties;
    }

    public void enviar(String para, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(recuperacionProperties.getRemitente());
        mensaje.setTo(para);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        mailSender.send(mensaje);
    }
}
