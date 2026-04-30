package cl.duocuc.sanossalvos.bff.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFound devuelve 404")
    void handleNotFound_returns404() {
        HttpClientErrorException.NotFound ex =
                (HttpClientErrorException.NotFound) HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null);

        ResponseEntity<Map<String, Object>> resp = handler.handleNotFound(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsKey("mensaje");
    }

    @Test
    @DisplayName("handleClientError devuelve 400 para errores 4xx")
    void handleClientError_returns400() {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", null, null, null);

        ResponseEntity<Map<String, Object>> resp = handler.handleClientError(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsKey("timestamp");
    }

    @Test
    @DisplayName("handleServiceUnavailable devuelve 503")
    void handleServiceUnavailable_returns503() {
        ResourceAccessException ex = new ResourceAccessException("connection refused");

        ResponseEntity<Map<String, Object>> resp = handler.handleServiceUnavailable(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(resp.getBody()).containsEntry("status", 503);
    }

    @Test
    @DisplayName("handleBadArgument devuelve 400 con el mensaje de la excepción")
    void handleBadArgument_returns400WithMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("reporte 99 no encontrado");

        ResponseEntity<Map<String, Object>> resp = handler.handleBadArgument(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsEntry("mensaje", "reporte 99 no encontrado");
    }

    @Test
    @DisplayName("handleGeneric devuelve 500")
    void handleGeneric_returns500() {
        Exception ex = new RuntimeException("unexpected");

        ResponseEntity<Map<String, Object>> resp = handler.handleGeneric(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody()).containsKey("error");
    }
}
