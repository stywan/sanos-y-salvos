package cl.duocuc.sanossalvos.matchingengine.kafka;

import cl.duocuc.sanossalvos.matchingengine.service.MatchingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteConsumerTest {

    @Mock MatchingService matchingService;
    @Spy  ObjectMapper    objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks ReporteConsumer consumer;

    private String payloadValido;

    @BeforeEach
    void setUp() {
        payloadValido = """
            {
              "reporteId": 1,
              "tipo": "PERDIDO",
              "usuarioId": 10,
              "especie": "Perro",
              "nombreMascota": "Max",
              "latitud": -33.45,
              "longitud": -70.65,
              "comuna": "Providencia",
              "emailContacto": "test@test.cl",
              "nombreContacto": "Juan",
              "fechaReporte": "2025-01-01T12:00:00"
            }
            """;
    }

    @Test
    @DisplayName("onReporteCreado: payload válido dispara buscarMatches")
    void onReporteCreado_payloadValido_disparaMatching() {
        when(matchingService.buscarMatches(1L)).thenReturn(List.of());

        consumer.onReporteCreado(payloadValido);

        verify(matchingService).buscarMatches(1L);
    }

    @Test
    @DisplayName("onReporteCreado: payload inválido no lanza excepción")
    void onReporteCreado_payloadInvalido_noLanzaExcepcion() {
        consumer.onReporteCreado("{ json: invalido }");

        verify(matchingService, never()).buscarMatches(anyLong());
    }
}
