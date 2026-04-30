package cl.duocuc.sanossalvos.bff.client;

import cl.duocuc.sanossalvos.bff.dto.ext.MatchDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MatchingEngineClient {

    private final RestClient restClient;

    public MatchingEngineClient(@Qualifier("matchingEngineRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Dispara la búsqueda de matches para un reporte.
     * Endpoint interno — no requiere JWT del usuario.
     */
    @CircuitBreaker(name = "matchingEngine", fallbackMethod = "buscarMatchesFallback")
    public List<MatchDto> buscarMatches(Long reporteId) {
        return restClient.post()
                .uri("/api/matching/buscar")
                .body(Map.of("reporteId", reporteId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<MatchDto>>() {});
    }

    public List<MatchDto> buscarMatchesFallback(Long reporteId, Throwable t) {
        log.warn("[CB] ms-matching-engine no disponible al buscar matches para reporte {}: {}",
                reporteId, t.getMessage());
        return List.of();
    }

    /** Obtiene todos los matches de un reporte dado (público). */
    @CircuitBreaker(name = "matchingEngine", fallbackMethod = "getMatchesPorReporteFallback")
    public List<MatchDto> getMatchesPorReporte(Long reporteId) {
        return restClient.get()
                .uri(uri -> uri.path("/api/matching/matches")
                        .queryParam("reporteId", reporteId)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<MatchDto>>() {});
    }

    public List<MatchDto> getMatchesPorReporteFallback(Long reporteId, Throwable t) {
        log.warn("[CB] ms-matching-engine no disponible al obtener matches del reporte {}: {}",
                reporteId, t.getMessage());
        return List.of();
    }
}
