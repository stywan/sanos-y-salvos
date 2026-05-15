package cl.duocuc.sanossalvos.notification.kafka;

import cl.duocuc.sanossalvos.notification.dto.CrearNotificacionRequest;
import cl.duocuc.sanossalvos.notification.dto.NotificacionResponse;
import cl.duocuc.sanossalvos.notification.kafka.event.AvistamientoRegistradoEvent;
import cl.duocuc.sanossalvos.notification.kafka.event.MatchEncontradoEvent;
import cl.duocuc.sanossalvos.notification.kafka.event.ReporteResueltoEvent;
import cl.duocuc.sanossalvos.notification.model.TipoNotificacion;
import cl.duocuc.sanossalvos.notification.service.NotificacionService;
import cl.duocuc.sanossalvos.notification.sse.SseEmitterRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume eventos de Kafka y los traduce en notificaciones persistidas + correos.
 * Cada listener corre en su propio hilo del container factory por defecto.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionConsumer {

    private final NotificacionService notificacionService;
    private final ObjectMapper objectMapper;
    private final SseEmitterRegistry sseEmitterRegistry;

    @KafkaListener(topics = KafkaTopics.AVISTAMIENTO_REGISTRADO, groupId = "ms-notification")
    public void onAvistamientoRegistrado(String payload) {
        try {
            AvistamientoRegistradoEvent ev = objectMapper.readValue(payload, AvistamientoRegistradoEvent.class);
            log.info("[Kafka←] avistamiento.registrado reporteId={} dueno={}", ev.reporteId(), ev.usuarioIdDueno());

            String mascota = ev.nombreMascota() != null ? ev.nombreMascota() : "tu mascota";
            CrearNotificacionRequest req = new CrearNotificacionRequest();
            req.setUsuarioId(ev.usuarioIdDueno());
            req.setTipo(TipoNotificacion.REPORTE_RESUELTO);
            req.setTitulo("¡Avistamiento de " + mascota + "!");
            req.setMensaje("Un usuario reportó haber visto a " + mascota +
                    (ev.comuna() != null ? " en " + ev.comuna() : "") +
                    ". Tu reporte fue marcado como resuelto.");
            req.setReporteId(ev.reporteId());
            req.setEmailDestino(ev.emailDueno());

            NotificacionResponse saved = notificacionService.crearNotificacion(req);
            sseEmitterRegistry.emitTo(ev.usuarioIdDueno(), "notificacion", saved);
        } catch (Exception e) {
            log.error("[Kafka←] Error procesando avistamiento.registrado: {} | payload={}", e.getMessage(), payload);
        }
    }

    @KafkaListener(topics = KafkaTopics.REPORTE_RESUELTO, groupId = "ms-notification")
    public void onReporteResuelto(String payload) {
        try {
            ReporteResueltoEvent ev = objectMapper.readValue(payload, ReporteResueltoEvent.class);
            log.info("[Kafka←] reporte.resuelto reporteId={} usuarioId={}", ev.reporteId(), ev.usuarioId());

            String mascota = ev.nombreMascota() != null ? ev.nombreMascota() : "Tu mascota";
            CrearNotificacionRequest req = new CrearNotificacionRequest();
            req.setUsuarioId(ev.usuarioId());
            req.setTipo(TipoNotificacion.REPORTE_RESUELTO);
            req.setTitulo("Reporte resuelto");
            req.setMensaje(mascota + " ha sido marcada como reunida. ¡Felicitaciones!");
            req.setReporteId(ev.reporteId());
            req.setEmailDestino(ev.emailContacto());

            NotificacionResponse saved = notificacionService.crearNotificacion(req);
            sseEmitterRegistry.emitTo(ev.usuarioId(), "notificacion", saved);
        } catch (Exception e) {
            log.error("[Kafka←] Error procesando reporte.resuelto: {} | payload={}", e.getMessage(), payload);
        }
    }

    @KafkaListener(topics = KafkaTopics.MATCH_ENCONTRADO, groupId = "ms-notification")
    public void onMatchEncontrado(String payload) {
        try {
            MatchEncontradoEvent ev = objectMapper.readValue(payload, MatchEncontradoEvent.class);
            log.info("[Kafka←] match.encontrado origen={} candidato={}", ev.reporteOrigenId(), ev.reporteCandidatoId());

            String mascota = ev.nombreMascota() != null ? ev.nombreMascota() : "tu mascota";
            String distancia = ev.distanciaKm() != null ? String.format(" a %.1f km de distancia", ev.distanciaKm()) : "";

            CrearNotificacionRequest req = new CrearNotificacionRequest();
            req.setUsuarioId(ev.usuarioIdNotificar());
            req.setTipo(TipoNotificacion.MATCH_ENCONTRADO);
            req.setTitulo("¡Posible coincidencia encontrada!");
            req.setMensaje("Encontramos un reporte que coincide con " + mascota + distancia + ". Revisa los detalles.");
            req.setReporteId(ev.reporteCandidatoId());
            req.setEmailDestino(ev.emailNotificar());

            NotificacionResponse saved = notificacionService.crearNotificacion(req);
            sseEmitterRegistry.emitTo(ev.usuarioIdNotificar(), "notificacion", saved);
        } catch (Exception e) {
            log.error("[Kafka←] Error procesando match.encontrado: {} | payload={}", e.getMessage(), payload);
        }
    }
}
