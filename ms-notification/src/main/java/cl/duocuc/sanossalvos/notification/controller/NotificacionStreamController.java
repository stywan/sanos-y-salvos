package cl.duocuc.sanossalvos.notification.controller;

import cl.duocuc.sanossalvos.notification.security.JwtUtil;
import cl.duocuc.sanossalvos.notification.sse.SseEmitterRegistry;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Stream SSE de notificaciones en tiempo real.
 *
 * El navegador no permite headers personalizados en EventSource, así que el JWT
 * llega por query param (?token=...).
 *
 * Este endpoint es 'permitAll' a nivel de Spring Security; la autenticación se hace
 * manualmente acá usando JwtUtil.
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Slf4j
public class NotificacionStreamController {

    /** Timeout: 30 minutos. El frontend reabre automáticamente al cerrar. */
    private static final long STREAM_TIMEOUT_MS = 30 * 60 * 1000L;

    private final SseEmitterRegistry sseEmitterRegistry;
    private final JwtUtil jwtUtil;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin(origins = "*")
    public ResponseEntity<SseEmitter> stream(@RequestParam("token") String token) {
        Long usuarioId;
        try {
            Claims claims = jwtUtil.parseToken(token);
            usuarioId = claims.get("userId", Long.class);
            if (usuarioId == null) {
                log.warn("[SSE] Token sin claim userId");
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            log.warn("[SSE] Token inválido: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }

        SseEmitter emitter = sseEmitterRegistry.register(usuarioId, STREAM_TIMEOUT_MS);
        return ResponseEntity.ok(emitter);
    }
}
