package cl.duocuc.sanossalvos.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mantiene un mapa de SseEmitter activos por usuarioId. Permite que el consumer Kafka
 * empuje notificaciones a todas las pestañas/sesiones abiertas de un usuario en tiempo real.
 *
 * Un usuario puede tener múltiples emitters activos (varias pestañas/dispositivos).
 */
@Component
@Slf4j
public class SseEmitterRegistry {

    private final Map<Long, List<SseEmitter>> emittersPorUsuario = new ConcurrentHashMap<>();

    public SseEmitter register(Long usuarioId, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);

        List<SseEmitter> lista = emittersPorUsuario.computeIfAbsent(usuarioId, k -> new CopyOnWriteArrayList<>());
        lista.add(emitter);
        log.info("[SSE] Cliente conectado usuarioId={} (total={})", usuarioId, lista.size());

        emitter.onCompletion(() -> remove(usuarioId, emitter, "completion"));
        emitter.onTimeout(() -> remove(usuarioId, emitter, "timeout"));
        emitter.onError(ex -> remove(usuarioId, emitter, "error: " + ex.getMessage()));

        // Mensaje inicial para confirmar conexión
        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of("usuarioId", usuarioId)));
        } catch (IOException e) {
            log.warn("[SSE] No se pudo enviar mensaje 'connected' a usuarioId={}: {}", usuarioId, e.getMessage());
        }
        return emitter;
    }

    public void emitTo(Long usuarioId, String eventName, Object payload) {
        List<SseEmitter> lista = emittersPorUsuario.get(usuarioId);
        if (lista == null || lista.isEmpty()) {
            log.debug("[SSE] No hay clientes conectados para usuarioId={}", usuarioId);
            return;
        }
        for (SseEmitter emitter : lista) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException e) {
                log.warn("[SSE] Error enviando a usuarioId={}: {}. Removiendo emitter.", usuarioId, e.getMessage());
                remove(usuarioId, emitter, "send-error");
            }
        }
    }

    private void remove(Long usuarioId, SseEmitter emitter, String reason) {
        List<SseEmitter> lista = emittersPorUsuario.get(usuarioId);
        if (lista != null) {
            lista.remove(emitter);
            log.info("[SSE] Cliente desconectado usuarioId={} reason={} restantes={}", usuarioId, reason, lista.size());
            if (lista.isEmpty()) emittersPorUsuario.remove(usuarioId);
        }
    }
}
