package cl.duocuc.sanossalvos.matchingengine.kafka;

import cl.duocuc.sanossalvos.matchingengine.kafka.event.MatchEncontradoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publicarMatchEncontrado(MatchEncontradoEvent event) {
        try {
            String key = String.valueOf(event.reporteOrigenId());
            kafkaTemplate.send(KafkaTopics.MATCH_ENCONTRADO, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("[Kafka] Error publicando match.encontrado: {}", ex.getMessage());
                        } else {
                            log.info("[Kafka] Publicado match.encontrado key={} offset={}",
                                    key, result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("[Kafka] Excepción al publicar match.encontrado: {}", e.getMessage());
        }
    }
}
