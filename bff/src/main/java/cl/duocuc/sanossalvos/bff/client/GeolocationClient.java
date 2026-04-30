package cl.duocuc.sanossalvos.bff.client;

import cl.duocuc.sanossalvos.bff.dto.ext.ZonaDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
public class GeolocationClient {

    private final RestClient restClient;

    public GeolocationClient(@Qualifier("geolocationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /** Obtiene las zonas de búsqueda activas del usuario autenticado. */
    @CircuitBreaker(name = "geolocation", fallbackMethod = "misZonasFallback")
    public List<ZonaDto> misZonas(String token) {
        return restClient.get()
                .uri("/api/geo/zonas")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ZonaDto>>() {});
    }

    public List<ZonaDto> misZonasFallback(String token, Throwable t) {
        log.warn("[CB] ms-geolocation no disponible al obtener zonas: {}", t.getMessage());
        return List.of();
    }
}
