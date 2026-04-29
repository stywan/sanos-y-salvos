package cl.duocuc.sanossalvos.notification.service;

import cl.duocuc.sanossalvos.notification.dto.CrearNotificacionRequest;
import cl.duocuc.sanossalvos.notification.dto.NotificacionResponse;
import cl.duocuc.sanossalvos.notification.exception.NotificacionNotFoundException;
import cl.duocuc.sanossalvos.notification.model.Notificacion;
import cl.duocuc.sanossalvos.notification.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final EmailService emailService;

    /**
     * Crea una notificación en BD y envía correo si se proporcionó emailDestino.
     * Llamado internamente por el matching engine.
     */
    @Transactional
    public NotificacionResponse crearNotificacion(CrearNotificacionRequest request) {
        Notificacion notificacion = Notificacion.builder()
                .usuarioId(request.getUsuarioId())
                .tipo(request.getTipo())
                .titulo(request.getTitulo())
                .mensaje(request.getMensaje())
                .reporteId(request.getReporteId())
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Notificacion guardada = notificacionRepository.save(notificacion);

        // Envío de correo opcional — no bloquea si falla
        if (request.getEmailDestino() != null && !request.getEmailDestino().isBlank()) {
            emailService.enviarCorreo(
                    request.getEmailDestino(),
                    request.getTitulo(),
                    request.getMensaje()
            );
        }

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> misNotificaciones(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public NotificacionResponse marcarComoLeida(Long id, Long usuarioId) {
        Notificacion notif = notificacionRepository.findById(id)
                .orElseThrow(() -> new NotificacionNotFoundException(id));

        if (!notif.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permiso para modificar esta notificación");
        }

        if (!notif.getLeida()) {
            notif.setLeida(true);
            notif.setFechaLeida(LocalDateTime.now());
            notificacionRepository.save(notif);
        }

        return toResponse(notif);
    }

    @Transactional
    public int marcarTodasComoLeidas(Long usuarioId) {
        return notificacionRepository.marcarTodasComoLeidas(usuarioId);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private NotificacionResponse toResponse(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .usuarioId(n.getUsuarioId())
                .tipo(n.getTipo())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .reporteId(n.getReporteId())
                .leida(n.getLeida())
                .fechaCreacion(n.getFechaCreacion())
                .fechaLeida(n.getFechaLeida())
                .build();
    }
}
