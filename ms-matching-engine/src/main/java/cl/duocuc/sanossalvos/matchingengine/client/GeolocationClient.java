package cl.duocuc.sanossalvos.matchingengine.client;

import cl.duocuc.sanossalvos.matchingengine.dto.ext.FiltrarCercanosRequest;
import cl.duocuc.sanossalvos.matchingengine.dto.ext.PuntoCercanoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
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

    @CircuitBreaker(name = "geolocation", fallbackMethod = "filtrarCercanosFallback")
    public List<PuntoCercanoDto> filtrarCercanos(FiltrarCercanosRequest request) {
        return restClient.post()
                .uri("/api/geo/filtrar-cercanos")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PuntoCercanoDto>>() {});
    }

    public List<PuntoCercanoDto> filtrarCercanosFallback(FiltrarCercanosRequest request, Throwable t) {
        log.warn("[CB] ms-geolocation no disponible: {}", t.getMessage());
        return List.of();
    }
}
