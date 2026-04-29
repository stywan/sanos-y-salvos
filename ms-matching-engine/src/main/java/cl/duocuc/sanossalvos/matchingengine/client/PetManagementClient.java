package cl.duocuc.sanossalvos.matchingengine.client;

import cl.duocuc.sanossalvos.matchingengine.dto.ext.ReporteDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class PetManagementClient {

    private final RestClient restClient;

    public PetManagementClient(@Qualifier("petManagementRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "petManagement", fallbackMethod = "obtenerReporteFallback")
    public Optional<ReporteDto> obtenerReporte(Long reporteId) {
        ReporteDto reporte = restClient.get()
                .uri("/api/pets/reportes/{id}", reporteId)
                .retrieve()
                .body(ReporteDto.class);
        return Optional.ofNullable(reporte);
    }

    public Optional<ReporteDto> obtenerReporteFallback(Long reporteId, Throwable t) {
        log.warn("[CB] ms-pet-management no disponible al obtener reporte {}: {}", reporteId, t.getMessage());
        return Optional.empty();
    }

    @CircuitBreaker(name = "petManagement", fallbackMethod = "listarReportesFallback")
    public List<ReporteDto> listarReportes(String tipo, String estado) {
        return restClient.get()
                .uri(uri -> uri.path("/api/pets/reportes")
                        .queryParam("tipo", tipo)
                        .queryParam("estado", estado)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ReporteDto>>() {});
    }

    public List<ReporteDto> listarReportesFallback(String tipo, String estado, Throwable t) {
        log.warn("[CB] ms-pet-management no disponible al listar reportes tipo={} estado={}: {}",
                tipo, estado, t.getMessage());
        return List.of();
    }
}
