package cl.duocuc.sanossalvos.bff.client;

import cl.duocuc.sanossalvos.bff.dto.ext.NotificacionDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NotificacionClient {

    private final RestClient restClient;

    public NotificacionClient(@Qualifier("notificationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /** Obtiene las notificaciones del usuario autenticado. */
    @CircuitBreaker(name = "notification", fallbackMethod = "misNotificacionesFallback")
    public List<NotificacionDto> misNotificaciones(String token) {
        return restClient.get()
                .uri("/api/notificaciones/mis-notificaciones")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<NotificacionDto>>() {});
    }

    public List<NotificacionDto> misNotificacionesFallback(String token, Throwable t) {
        log.warn("[CB] ms-notification no disponible al obtener notificaciones: {}", t.getMessage());
        return List.of();
    }

    /**
     * Cuenta las notificaciones no leídas del usuario.
     * Retorna 0 si el servicio no está disponible (degraded mode).
     */
    @CircuitBreaker(name = "notification", fallbackMethod = "contarNoLeidasFallback")
    public long contarNoLeidas(String token) {
        Map<String, Long> respuesta = restClient.get()
                .uri("/api/notificaciones/no-leidas/count")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Long>>() {});
        return respuesta != null ? respuesta.getOrDefault("noLeidas", 0L) : 0L;
    }

    public long contarNoLeidasFallback(String token, Throwable t) {
        log.warn("[CB] ms-notification no disponible al contar no leídas: {}", t.getMessage());
        return 0L;
    }
}
