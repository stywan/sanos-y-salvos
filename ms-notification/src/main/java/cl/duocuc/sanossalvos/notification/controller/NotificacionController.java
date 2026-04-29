package cl.duocuc.sanossalvos.notification.controller;

import cl.duocuc.sanossalvos.notification.dto.CrearNotificacionRequest;
import cl.duocuc.sanossalvos.notification.dto.NotificacionResponse;
import cl.duocuc.sanossalvos.notification.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    /**
     * Crear notificación (interno — sin JWT de usuario, llamado por ms-matching-engine).
     */
    @PostMapping
    public ResponseEntity<NotificacionResponse> crearNotificacion(
            @Valid @RequestBody CrearNotificacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificacionService.crearNotificacion(request));
    }

    /** Listar mis notificaciones ordenadas de más reciente a más antigua (requiere JWT). */
    @GetMapping("/mis-notificaciones")
    public ResponseEntity<List<NotificacionResponse>> misNotificaciones(Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        return ResponseEntity.ok(notificacionService.misNotificaciones(usuarioId));
    }

    /** Cantidad de notificaciones no leídas (para la campana del frontend, requiere JWT). */
    @GetMapping("/no-leidas/count")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.contarNoLeidas(usuarioId)));
    }

    /** Marcar una notificación como leída (requiere JWT y ser el dueño). */
    @PatchMapping("/{id}/leer")
    public ResponseEntity<NotificacionResponse> marcarComoLeida(@PathVariable Long id,
                                                                Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id, usuarioId));
    }

    /** Marcar todas las notificaciones como leídas (requiere JWT). */
    @PatchMapping("/leer-todas")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLeidas(Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        int actualizadas = notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok(Map.of("actualizadas", actualizadas));
    }
}
