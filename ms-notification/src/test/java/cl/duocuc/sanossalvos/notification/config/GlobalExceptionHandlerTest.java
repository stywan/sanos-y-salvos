package cl.duocuc.sanossalvos.notification.config;

import cl.duocuc.sanossalvos.notification.exception.NotificacionNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFound: retorna 404")
    void handleNotFound_returns404() {
        var ex = new NotificacionNotFoundException(99L);
        ResponseEntity<Map<String, Object>> resp = handler.handleNotFound(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsEntry("status", 404);
        assertThat(resp.getBody().get("mensaje").toString()).contains("Notificación");
    }

    @Test
    @DisplayName("handleBadArgument: retorna 400")
    void handleBadArgument_returns400() {
        var ex = new IllegalArgumentException("parámetro inválido");
        ResponseEntity<Map<String, Object>> resp = handler.handleBadArgument(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsEntry("mensaje", "parámetro inválido");
    }

    @Test
    @DisplayName("buildError incluye timestamp, status y error en el body")
    void buildError_includesRequiredFields() {
        var ex = new IllegalArgumentException("test");
        ResponseEntity<Map<String, Object>> resp = handler.handleBadArgument(ex);

        assertThat(resp.getBody()).containsKeys("timestamp", "status", "error", "mensaje");
    }
}
