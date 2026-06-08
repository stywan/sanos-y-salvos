package cl.duocuc.sanossalvos.petmanagement.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFound: retorna 404")
    void handleNotFound_returns404() {
        var ex = new ReporteNotFoundException(99L);
        ResponseEntity<ErrorResponse> resp = handler.handleNotFound(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().getStatus()).isEqualTo(404);
        assertThat(resp.getBody().getMessage()).contains("99");
    }

    @Test
    @DisplayName("handleBadRequest: retorna 400 con mensaje")
    void handleBadRequest_returns400() {
        var ex = new IllegalArgumentException("argumento inválido");
        ResponseEntity<ErrorResponse> resp = handler.handleBadRequest(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().getMessage()).isEqualTo("argumento inválido");
    }

    @Test
    @DisplayName("handleValidation: retorna 400 con el primer campo inválido")
    void handleValidation_returns400() throws Exception {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "nombreMascota", "no puede estar vacío"));
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> resp = handler.handleValidation(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().getMessage()).contains("nombreMascota");
    }

    @Test
    @DisplayName("handleGeneric: retorna 500")
    void handleGeneric_returns500() {
        var ex = new RuntimeException("error inesperado");
        ResponseEntity<ErrorResponse> resp = handler.handleGeneric(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().getStatus()).isEqualTo(500);
    }
}
