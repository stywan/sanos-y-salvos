package cl.duocuc.sanossalvos.matchingengine.kafka;

import cl.duocuc.sanossalvos.matchingengine.kafka.event.MatchEncontradoEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchEventPublisherTest {

    @Mock KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks MatchEventPublisher publisher;

    private MatchEncontradoEvent buildEvent() {
        return new MatchEncontradoEvent(1L, 2L, 10L, "owner@test.cl", "Max", 3.5, 80.0, LocalDateTime.now());
    }

    @Test
    @DisplayName("publicarMatchEncontrado: envía a Kafka correctamente")
    void publicarMatchEncontrado_enviaAKafka() {
        RecordMetadata meta = new RecordMetadata(
                new TopicPartition(KafkaTopics.MATCH_ENCONTRADO, 0), 0L, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(meta);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        publisher.publicarMatchEncontrado(buildEvent());

        verify(kafkaTemplate).send(eq(KafkaTopics.MATCH_ENCONTRADO), anyString(), any());
    }

    @Test
    @DisplayName("publicarMatchEncontrado: excepción en KafkaTemplate no se propaga")
    void publicarMatchEncontrado_kafkaFalla_noLanzaExcepcion() {
        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka down"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failedFuture);

        publisher.publicarMatchEncontrado(buildEvent());

        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }
}
