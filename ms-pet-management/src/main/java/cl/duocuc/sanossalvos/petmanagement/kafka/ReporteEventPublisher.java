package cl.duocuc.sanossalvos.petmanagement.kafka;

import cl.duocuc.sanossalvos.petmanagement.kafka.event.AvistamientoRegistradoEvent;
import cl.duocuc.sanossalvos.petmanagement.kafka.event.ReporteCreadoEvent;
import cl.duocuc.sanossalvos.petmanagement.kafka.event.ReporteResueltoEvent;
import cl.duocuc.sanossalvos.petmanagement.model.Reporte;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Wrapper sobre KafkaTemplate para publicar eventos de dominio.
 * Los envíos son no-bloqueantes y NO fallan la transacción de negocio si Kafka cae.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publicarReporteCreado(Reporte reporte) {
        String especie = (reporte.getMascota() != null && reporte.getMascota().getEspecie() != null)
                ? reporte.getMascota().getEspecie().getNombre() : null;
        String nombreMascota = (reporte.getMascota() != null) ? reporte.getMascota().getNombre() : null;

        ReporteCreadoEvent event = new ReporteCreadoEvent(
                reporte.getId(),
                reporte.getTipo().name(),
                reporte.getUsuarioId(),
                especie,
                nombreMascota,
                reporte.getLatitud(),
                reporte.getLongitud(),
                reporte.getComuna(),
                reporte.getEmailContacto(),
                reporte.getNombreContacto(),
                reporte.getFechaReporte()
        );
        publish(KafkaTopics.REPORTE_CREADO, String.valueOf(reporte.getId()), event);
    }

    /**
     * @param reporteOriginal el reporte PERDIDO sobre el que se registró el avistamiento
     * @param usuarioIdReportador id del usuario que registró el avistamiento (no es el dueño)
     */
    public void publicarAvistamiento(Reporte reporteOriginal, Long usuarioIdReportador) {
        String especie = (reporteOriginal.getMascota() != null && reporteOriginal.getMascota().getEspecie() != null)
                ? reporteOriginal.getMascota().getEspecie().getNombre() : null;
        String nombreMascota = (reporteOriginal.getMascota() != null) ? reporteOriginal.getMascota().getNombre() : null;

        AvistamientoRegistradoEvent event = new AvistamientoRegistradoEvent(
                reporteOriginal.getId(),
                reporteOriginal.getUsuarioId(),
                usuarioIdReportador,
                reporteOriginal.getEmailContacto(),
                nombreMascota,
                especie,
                reporteOriginal.getComuna(),
                LocalDateTime.now()
        );
        publish(KafkaTopics.AVISTAMIENTO_REGISTRADO, String.valueOf(reporteOriginal.getId()), event);
    }

    public void publicarReporteResuelto(Reporte reporte) {
        String nombreMascota = (reporte.getMascota() != null) ? reporte.getMascota().getNombre() : null;

        ReporteResueltoEvent event = new ReporteResueltoEvent(
                reporte.getId(),
                reporte.getUsuarioId(),
                reporte.getTipo().name(),
                reporte.getEmailContacto(),
                nombreMascota,
                LocalDateTime.now()
        );
        publish(KafkaTopics.REPORTE_RESUELTO, String.valueOf(reporte.getId()), event);
    }

    /**
     * Envío fire-and-forget con logging. Si Kafka falla, se loguea pero NO se propaga la excepción
     * para no romper la transacción de negocio (eventual consistency).
     */
    private void publish(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[Kafka] Error publicando en topic={} key={}: {}", topic, key, ex.getMessage());
                } else {
                    log.info("[Kafka] Publicado topic={} key={} offset={}", topic, key,
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("[Kafka] Excepción al intentar publicar en topic={}: {}", topic, e.getMessage());
        }
    }
}
