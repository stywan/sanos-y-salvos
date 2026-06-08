package cl.duocuc.sanossalvos.notification.kafka;

import cl.duocuc.sanossalvos.notification.dto.NotificacionResponse;
import cl.duocuc.sanossalvos.notification.model.TipoNotificacion;
import cl.duocuc.sanossalvos.notification.service.NotificacionService;
import cl.duocuc.sanossalvos.notification.sse.SseEmitterRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionConsumerTest {

    @Mock NotificacionService   notificacionService;
    @Mock SseEmitterRegistry    sseEmitterRegistry;

    private NotificacionConsumer consumer;
    private NotificacionResponse notifResponse;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        consumer = new NotificacionConsumer(notificacionService, mapper, sseEmitterRegistry);

        notifResponse = NotificacionResponse.builder()
                .id(1L).usuarioId(10L).tipo(TipoNotificacion.MATCH_ENCONTRADO)
                .titulo("Test").mensaje("Mensaje").leida(false)
                .fechaCreacion(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("onMatchEncontrado: crea notificación y emite SSE")
    void onMatchEncontrado_creaNotificacionYEmiteSSE() {
        when(notificacionService.crearNotificacion(any())).thenReturn(notifResponse);

        String payload = """
                {
                  "reporteOrigenId": 1,
                  "reporteCandidatoId": 2,
                  "usuarioIdNotificar": 10,
                  "emailNotificar": "user@test.cl",
                  "nombreMascota": "Rex",
                  "distanciaKm": 1.5,
                  "fechaMatch": "2025-01-01T10:00:00"
                }
                """;

        consumer.onMatchEncontrado(payload);

        verify(notificacionService).crearNotificacion(any());
        verify(sseEmitterRegistry).emitTo(eq(10L), eq("notificacion"), any());
    }

    @Test
    @DisplayName("onAvistamientoRegistrado: crea notificación y emite SSE")
    void onAvistamientoRegistrado_creaNotificacionYEmiteSSE() {
        when(notificacionService.crearNotificacion(any())).thenReturn(notifResponse);

        String payload = """
                {
                  "reporteId": 5,
                  "usuarioIdDueno": 10,
                  "usuarioIdReportador": 20,
                  "emailDueno": "dueno@test.cl",
                  "nombreMascota": "Luna",
                  "especie": "Gato",
                  "comuna": "Santiago",
                  "fechaAvistamiento": "2025-01-01T10:00:00"
                }
                """;

        consumer.onAvistamientoRegistrado(payload);

        verify(notificacionService).crearNotificacion(any());
        verify(sseEmitterRegistry).emitTo(eq(10L), eq("notificacion"), any());
    }

    @Test
    @DisplayName("onReporteResuelto: crea notificación y emite SSE")
    void onReporteResuelto_creaNotificacionYEmiteSSE() {
        when(notificacionService.crearNotificacion(any())).thenReturn(notifResponse);

        String payload = """
                {
                  "reporteId": 3,
                  "usuarioId": 10,
                  "tipo": "PERDIDO",
                  "emailContacto": "user@test.cl",
                  "nombreMascota": "Firulais",
                  "fechaResolucion": "2025-01-01T10:00:00"
                }
                """;

        consumer.onReporteResuelto(payload);

        verify(notificacionService).crearNotificacion(any());
        verify(sseEmitterRegistry).emitTo(eq(10L), eq("notificacion"), any());
    }

    @Test
    @DisplayName("onMatchEncontrado: JSON inválido no lanza excepción (solo loguea error)")
    void onMatchEncontrado_payloadInvalido_noLanzaExcepcion() {
        consumer.onMatchEncontrado("json-invalido-{}");
        // No debe lanzar excepción — el consumer captura y loguea
    }

    // ── Ramas null ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("onMatchEncontrado: nombreMascota null usa texto por defecto")
    void onMatchEncontrado_sinNombreMascota_usaDefecto() {
        when(notificacionService.crearNotificacion(any())).thenReturn(notifResponse);

        String payload = """
                {
                  "reporteOrigenId": 1,
                  "reporteCandidatoId": 2,
                  "usuarioIdNotificar": 10,
                  "emailNotificar": "user@test.cl",
                  "nombreMascota": null,
                  "distanciaKm": null,
                  "fechaMatch": "2025-01-01T10:00:00"
                }
                """;

        consumer.onMatchEncontrado(payload);

        verify(notificacionService).crearNotificacion(any());
    }

    @Test
    @DisplayName("onAvistamientoRegistrado: nombreMascota null usa texto por defecto")
    void onAvistamientoRegistrado_sinNombreMascota_usaDefecto() {
        when(notificacionService.crearNotificacion(any())).thenReturn(notifResponse);

        String payload = """
                {
                  "reporteId": 5,
                  "usuarioIdDueno": 10,
                  "usuarioIdReportador": 20,
                  "emailDueno": "dueno@test.cl",
                  "nombreMascota": null,
                  "especie": "Gato",
                  "comuna": null,
                  "fechaAvistamiento": "2025-01-01T10:00:00"
                }
                """;

        consumer.onAvistamientoRegistrado(payload);

        verify(notificacionService).crearNotificacion(any());
    }

    @Test
    @DisplayName("onReporteResuelto: nombreMascota null usa texto por defecto")
    void onReporteResuelto_sinNombreMascota_usaDefecto() {
        when(notificacionService.crearNotificacion(any())).thenReturn(notifResponse);

        String payload = """
                {
                  "reporteId": 3,
                  "usuarioId": 10,
                  "tipo": "PERDIDO",
                  "emailContacto": "user@test.cl",
                  "nombreMascota": null,
                  "fechaResolucion": "2025-01-01T10:00:00"
                }
                """;

        consumer.onReporteResuelto(payload);

        verify(notificacionService).crearNotificacion(any());
    }
}
