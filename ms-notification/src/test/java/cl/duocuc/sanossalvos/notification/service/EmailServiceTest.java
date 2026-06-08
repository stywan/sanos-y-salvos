package cl.duocuc.sanossalvos.notification.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @InjectMocks EmailService emailService;

    @Test
    @DisplayName("enviarCorreo: invoca mailSender correctamente")
    void enviarCorreo_exitoso_llamaMailSender() {
        emailService.enviarCorreo("dest@test.cl", "Asunto", "Cuerpo del mensaje");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("enviarCorreo: excepción en mailSender no se propaga")
    void enviarCorreo_mailSenderFalla_noLanzaExcepcion() {
        doThrow(new RuntimeException("SMTP no disponible"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        emailService.enviarCorreo("dest@test.cl", "Asunto", "Cuerpo");
        // No debe lanzar excepción
    }
}
