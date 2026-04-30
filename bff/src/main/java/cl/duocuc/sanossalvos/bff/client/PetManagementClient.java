package cl.duocuc.sanossalvos.bff.client;

import cl.duocuc.sanossalvos.bff.dto.ext.ReporteDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
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

    /** Lista todos los reportes en estado ACTIVO (para el mapa). */
    @CircuitBreaker(name = "petManagement", fallbackMethod = "listarReportesActivosFallback")
    public List<ReporteDto> listarReportesActivos(String token) {
        return restClient.get()
                .uri(uri -> uri.path("/api/pets/reportes")
                        .queryParam("estado", "ACTIVO")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ReporteDto>>() {});
    }

    public List<ReporteDto> listarReportesActivosFallback(String token, Throwable t) {
        log.warn("[CB] ms-pet-management no disponible al listar reportes activos: {}", t.getMessage());
        return List.of();
    }

    /** Obtiene los reportes del usuario autenticado. */
    @CircuitBreaker(name = "petManagement", fallbackMethod = "misReportesFallback")
    public List<ReporteDto> misReportes(String token) {
        return restClient.get()
                .uri("/api/pets/reportes/mis-reportes")
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ReporteDto>>() {});
    }

    public List<ReporteDto> misReportesFallback(String token, Throwable t) {
        log.warn("[CB] ms-pet-management no disponible al obtener mis-reportes: {}", t.getMessage());
        return List.of();
    }

    /** Obtiene el detalle de un reporte por ID. */
    @CircuitBreaker(name = "petManagement", fallbackMethod = "obtenerReporteFallback")
    public Optional<ReporteDto> obtenerReporte(Long id, String token) {
        ReporteDto reporte = restClient.get()
                .uri("/api/pets/reportes/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .body(ReporteDto.class);
        return Optional.ofNullable(reporte);
    }

    public Optional<ReporteDto> obtenerReporteFallback(Long id, String token, Throwable t) {
        log.warn("[CB] ms-pet-management no disponible al obtener reporte {}: {}", id, t.getMessage());
        return Optional.empty();
    }
}
