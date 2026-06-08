package cl.duocuc.sanossalvos.notification.controller;

import cl.duocuc.sanossalvos.notification.dto.NotificacionResponse;
import cl.duocuc.sanossalvos.notification.model.TipoNotificacion;
import cl.duocuc.sanossalvos.notification.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock NotificacionService notificacionService;
    @InjectMocks NotificacionController controller;

    private Authentication auth;
    private NotificacionResponse notifResponse;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken("user@test.cl", 10L, List.of());
        notifResponse = NotificacionResponse.builder()
                .id(1L).usuarioId(10L).tipo(TipoNotificacion.MATCH_ENCONTRADO)
                .titulo("Match encontrado").mensaje("Hay una coincidencia")
                .leida(false).fechaCreacion(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("misNotificaciones: retorna 200 con lista")
    void misNotificaciones_returns200() {
        when(notificacionService.misNotificaciones(10L)).thenReturn(List.of(notifResponse));

        ResponseEntity<List<NotificacionResponse>> resp = controller.misNotificaciones(auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        verify(notificacionService).misNotificaciones(10L);
    }

    @Test
    @DisplayName("contarNoLeidas: retorna 200 con conteo")
    void contarNoLeidas_returns200WithCount() {
        when(notificacionService.contarNoLeidas(10L)).thenReturn(3L);

        ResponseEntity<Map<String, Long>> resp = controller.contarNoLeidas(auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("noLeidas")).isEqualTo(3L);
    }

    @Test
    @DisplayName("marcarComoLeida: retorna 200 con notificación actualizada")
    void marcarComoLeida_returns200() {
        NotificacionResponse leida = NotificacionResponse.builder()
                .id(1L).leida(true).fechaCreacion(LocalDateTime.now()).build();
        when(notificacionService.marcarComoLeida(1L, 10L)).thenReturn(leida);

        ResponseEntity<NotificacionResponse> resp = controller.marcarComoLeida(1L, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getLeida()).isTrue();
    }

    @Test
    @DisplayName("marcarTodasComoLeidas: retorna 200 con cantidad actualizada")
    void marcarTodasComoLeidas_returns200() {
        when(notificacionService.marcarTodasComoLeidas(10L)).thenReturn(5);

        ResponseEntity<Map<String, Integer>> resp = controller.marcarTodasComoLeidas(auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("actualizadas")).isEqualTo(5);
    }
}
