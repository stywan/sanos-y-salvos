package cl.duocuc.sanossalvos.notification.service;

import cl.duocuc.sanossalvos.notification.dto.CrearNotificacionRequest;
import cl.duocuc.sanossalvos.notification.dto.NotificacionResponse;
import cl.duocuc.sanossalvos.notification.exception.NotificacionNotFoundException;
import cl.duocuc.sanossalvos.notification.model.Notificacion;
import cl.duocuc.sanossalvos.notification.model.TipoNotificacion;
import cl.duocuc.sanossalvos.notification.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock NotificacionRepository notificacionRepository;
    @Mock EmailService emailService;
    @InjectMocks NotificacionService service;

    private Notificacion notifEjemplo;

    @BeforeEach
    void setUp() {
        notifEjemplo = Notificacion.builder()
                .id(1L)
                .usuarioId(10L)
                .tipo(TipoNotificacion.MATCH_ENCONTRADO)
                .titulo("Posible match encontrado")
                .mensaje("Se encontró una mascota que podría ser la tuya.")
                .reporteId(42L)
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    // ── crearNotificacion ─────────────────────────────────────────────────────

    @Test
    void crearNotificacion_guardaEnBD() {
        when(notificacionRepository.save(any())).thenReturn(notifEjemplo);

        CrearNotificacionRequest req = new CrearNotificacionRequest();
        req.setUsuarioId(10L);
        req.setTipo(TipoNotificacion.MATCH_ENCONTRADO);
        req.setTitulo("Posible match encontrado");
        req.setMensaje("Se encontró una mascota que podría ser la tuya.");
        req.setReporteId(42L);

        NotificacionResponse resp = service.crearNotificacion(req);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getLeida()).isFalse();
        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    void crearNotificacion_conEmail_enviaCorreo() {
        when(notificacionRepository.save(any())).thenReturn(notifEjemplo);

        CrearNotificacionRequest req = new CrearNotificacionRequest();
        req.setUsuarioId(10L);
        req.setTipo(TipoNotificacion.MATCH_ENCONTRADO);
        req.setTitulo("Match");
        req.setMensaje("Mensaje de prueba");
        req.setEmailDestino("usuario@ejemplo.com");

        service.crearNotificacion(req);

        verify(emailService).enviarCorreo(eq("usuario@ejemplo.com"), eq("Match"), eq("Mensaje de prueba"));
    }

    @Test
    void crearNotificacion_sinEmail_noEnviaCorreo() {
        when(notificacionRepository.save(any())).thenReturn(notifEjemplo);

        CrearNotificacionRequest req = new CrearNotificacionRequest();
        req.setUsuarioId(10L);
        req.setTipo(TipoNotificacion.ZONA_ALERTA);
        req.setTitulo("Alerta de zona");
        req.setMensaje("Hay una mascota en tu zona.");

        service.crearNotificacion(req);

        verifyNoInteractions(emailService);
    }

    // ── misNotificaciones ─────────────────────────────────────────────────────

    @Test
    void misNotificaciones_devuelveLasDelUsuario() {
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(10L))
                .thenReturn(List.of(notifEjemplo));

        List<NotificacionResponse> result = service.misNotificaciones(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTipo()).isEqualTo(TipoNotificacion.MATCH_ENCONTRADO);
    }

    // ── contarNoLeidas ────────────────────────────────────────────────────────

    @Test
    void contarNoLeidas_devuelveNumero() {
        when(notificacionRepository.countByUsuarioIdAndLeidaFalse(10L)).thenReturn(3L);
        assertThat(service.contarNoLeidas(10L)).isEqualTo(3L);
    }

    // ── marcarComoLeida ───────────────────────────────────────────────────────

    @Test
    void marcarComoLeida_actualizaEstado() {
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notifEjemplo));
        when(notificacionRepository.save(any())).thenReturn(notifEjemplo);

        NotificacionResponse resp = service.marcarComoLeida(1L, 10L);

        assertThat(notifEjemplo.getLeida()).isTrue();
        assertThat(notifEjemplo.getFechaLeida()).isNotNull();
    }

    @Test
    void marcarComoLeida_noDueno_lanzaException() {
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notifEjemplo));

        assertThatThrownBy(() -> service.marcarComoLeida(1L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permiso");
    }

    @Test
    void marcarComoLeida_noExiste_lanzaNotFoundException() {
        when(notificacionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarComoLeida(999L, 10L))
                .isInstanceOf(NotificacionNotFoundException.class);
    }

    // ── marcarTodasComoLeidas ─────────────────────────────────────────────────

    @Test
    void marcarTodasComoLeidas_retornaCantidadActualizada() {
        when(notificacionRepository.marcarTodasComoLeidas(10L)).thenReturn(3);
        assertThat(service.marcarTodasComoLeidas(10L)).isEqualTo(3);
    }
}
