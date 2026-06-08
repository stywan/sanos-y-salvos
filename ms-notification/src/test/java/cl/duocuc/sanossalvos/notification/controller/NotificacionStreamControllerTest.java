package cl.duocuc.sanossalvos.notification.controller;

import cl.duocuc.sanossalvos.notification.security.JwtUtil;
import cl.duocuc.sanossalvos.notification.sse.SseEmitterRegistry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionStreamControllerTest {

    @Mock JwtUtil            jwtUtil;
    @Mock SseEmitterRegistry sseEmitterRegistry;
    @Mock Claims             claims;

    @InjectMocks NotificacionStreamController controller;

    @Test
    @DisplayName("stream: token válido retorna 200 con SseEmitter")
    void stream_tokenValido_returns200() {
        SseEmitter emitter = new SseEmitter(60_000L);

        when(jwtUtil.parseToken("validtoken")).thenReturn(claims);
        when(claims.get("userId", Long.class)).thenReturn(7L);
        when(sseEmitterRegistry.register(7L, 30 * 60 * 1000L)).thenReturn(emitter);

        ResponseEntity<SseEmitter> resp = controller.stream("validtoken");

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(emitter);
    }

    @Test
    @DisplayName("stream: token sin userId retorna 401")
    void stream_tokenSinUserId_returns401() {
        when(jwtUtil.parseToken("nouserid")).thenReturn(claims);
        when(claims.get("userId", Long.class)).thenReturn(null);

        ResponseEntity<SseEmitter> resp = controller.stream("nouserid");

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(sseEmitterRegistry, never()).register(anyLong(), anyLong());
    }

    @Test
    @DisplayName("stream: token inválido retorna 401")
    void stream_tokenInvalido_returns401() {
        when(jwtUtil.parseToken("badtoken")).thenThrow(new JwtException("invalid"));

        ResponseEntity<SseEmitter> resp = controller.stream("badtoken");

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(sseEmitterRegistry, never()).register(anyLong(), anyLong());
    }
}
