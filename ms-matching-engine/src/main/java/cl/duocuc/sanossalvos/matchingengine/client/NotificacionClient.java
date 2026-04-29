package cl.duocuc.sanossalvos.matchingengine.client;

import cl.duocuc.sanossalvos.matchingengine.dto.ext.CrearNotificacionRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class NotificacionClient {

    private final RestClient restClient;

    public NotificacionClient(@Qualifier("notificationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "notification", fallbackMethod = "crearNotificacionFallback")
    public void crearNotificacion(CrearNotificacionRequest request) {
        restClient.post()
                .uri("/api/notificaciones")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void crearNotificacionFallback(CrearNotificacionRequest request, Throwable t) {
        log.warn("[CB] ms-notification no disponible — notificación no enviada a usuarioId={}: {}",
                request.getUsuarioId(), t.getMessage());
    }
}
