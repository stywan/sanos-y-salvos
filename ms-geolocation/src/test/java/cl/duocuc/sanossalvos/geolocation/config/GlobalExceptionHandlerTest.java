package cl.duocuc.sanossalvos.geolocation.config;

import cl.duocuc.sanossalvos.geolocation.exception.ZonaNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFound: retorna 404 con mensaje de zona")
    void handleNotFound_returns404() {
        var ex = new ZonaNotFoundException(99L);
        ResponseEntity<Map<String, Object>> resp = handler.handleNotFound(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsEntry("status", 404);
        assertThat(resp.getBody().get("mensaje").toString()).contains("99");
    }

    @Test
    @DisplayName("handleBadArgument: retorna 400 con mensaje")
    void handleBadArgument_returns400() {
        var ex = new IllegalArgumentException("parámetro inválido");
        ResponseEntity<Map<String, Object>> resp = handler.handleBadArgument(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsEntry("mensaje", "parámetro inválido");
    }

    @Test
    @DisplayName("handleValidation: retorna 400 con campo inválido")
    void handleValidation_returns400() throws Exception {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "radioKm", "debe ser positivo"));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> resp = handler.handleValidation(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().get("mensaje").toString()).contains("radioKm");
    }

    @Test
    @DisplayName("body incluye timestamp, status, error y mensaje")
    void body_incluyeCamposRequeridos() {
        var resp = handler.handleBadArgument(new IllegalArgumentException("test"));
        assertThat(resp.getBody()).containsKeys("timestamp", "status", "error", "mensaje");
    }
}
