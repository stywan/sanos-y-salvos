package cl.duocuc.sanossalvos.userauth.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleEmailYaRegistrado: retorna 409")
    void handleEmailYaRegistrado_returns409() {
        var ex = new EmailYaRegistradoException("test@test.cl");
        ResponseEntity<ErrorResponse> resp = handler.handleEmailYaRegistrado(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("handleCredencialesInvalidas BadCredentials: retorna 401")
    void handleBadCredentials_returns401() {
        var ex = new BadCredentialsException("credenciales inválidas");
        ResponseEntity<ErrorResponse> resp = handler.handleCredencialesInvalidas(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody().getError()).isEqualTo("No autorizado");
    }

    @Test
    @DisplayName("handleCredencialesInvalidas UsernameNotFound: retorna 401")
    void handleUsernameNotFound_returns401() {
        var ex = new UsernameNotFoundException("no encontrado");
        ResponseEntity<ErrorResponse> resp = handler.handleCredencialesInvalidas(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("handleIllegalArgument: retorna 400 con mensaje")
    void handleIllegalArgument_returns400() {
        var ex = new IllegalArgumentException("parámetro inválido");
        ResponseEntity<ErrorResponse> resp = handler.handleIllegalArgument(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().getMessage()).isEqualTo("parámetro inválido");
    }

    @Test
    @DisplayName("handleGeneral: retorna 500")
    void handleGeneral_returns500() {
        var ex = new RuntimeException("error inesperado");
        ResponseEntity<ErrorResponse> resp = handler.handleGeneral(ex);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().getStatus()).isEqualTo(500);
    }
}
