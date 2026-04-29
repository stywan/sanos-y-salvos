package cl.duocuc.sanossalvos.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Intenta enviar un correo. Si el servidor SMTP no está disponible (desarrollo local)
     * solo registra un warning y continúa — nunca bloquea la creación de la notificación.
     */
    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("[Sanos y Salvos] " + asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Correo enviado a {}", destinatario);
        } catch (Exception e) {
            log.warn("No se pudo enviar correo a {} — {}", destinatario, e.getMessage());
        }
    }
}
