package cl.duocuc.sanossalvos.matchingengine.kafka;

import cl.duocuc.sanossalvos.matchingengine.kafka.event.ReporteCreadoEvent;
import cl.duocuc.sanossalvos.matchingengine.service.MatchingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Reacciona al evento reporte.creado disparando el matching engine de forma asíncrona.
 * Reemplaza la llamada HTTP sincrónica que antes hacía el BFF a /api/matching/buscar.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteConsumer {

    private final MatchingService matchingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.REPORTE_CREADO, groupId = "ms-matching-engine")
    public void onReporteCreado(String payload) {
        try {
            ReporteCreadoEvent ev = objectMapper.readValue(payload, ReporteCreadoEvent.class);
            log.info("[Kafka←] reporte.creado id={} tipo={} — disparando matching", ev.reporteId(), ev.tipo());

            // El MatchingService ya publicará match.encontrado por cada coincidencia (vía MatchEventPublisher)
            int found = matchingService.buscarMatches(ev.reporteId()).size();
            log.info("[Kafka] Matching completado para reporte {}: {} matches", ev.reporteId(), found);
        } catch (Exception e) {
            log.error("[Kafka←] Error procesando reporte.creado: {} | payload={}", e.getMessage(), payload);
        }
    }
}
