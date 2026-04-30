package cl.duocuc.sanossalvos.bff.client;

import cl.duocuc.sanossalvos.bff.dto.ext.MatchDto;
import cl.duocuc.sanossalvos.bff.dto.ext.NotificacionDto;
import cl.duocuc.sanossalvos.bff.dto.ext.ReporteDto;
import cl.duocuc.sanossalvos.bff.dto.ext.ZonaDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Prueba los métodos de fallback de los Circuit Breakers.
 * Al estar el CB abierto se llaman directamente; deben devolver respuestas vacías sin lanzar excepciones.
 */
class ClientFallbackTest {

    private static final String TOKEN = "Bearer eyJtest";
    private static final RuntimeException CAUSE = new RuntimeException("service down");

    // -----------------------------------------------------------------------
    // PetManagementClient fallbacks
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PetManagementClient.listarReportesActivosFallback: devuelve lista vacía")
    void petManagement_listarReportesActivosFallback() {
        PetManagementClient client = new PetManagementClient(mock(RestClient.class));

        List<ReporteDto> result = client.listarReportesActivosFallback(TOKEN, CAUSE);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("PetManagementClient.misReportesFallback: devuelve lista vacía")
    void petManagement_misReportesFallback() {
        PetManagementClient client = new PetManagementClient(mock(RestClient.class));

        List<ReporteDto> result = client.misReportesFallback(TOKEN, CAUSE);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("PetManagementClient.obtenerReporteFallback: devuelve Optional vacío")
    void petManagement_obtenerReporteFallback() {
        PetManagementClient client = new PetManagementClient(mock(RestClient.class));

        Optional<ReporteDto> result = client.obtenerReporteFallback(1L, TOKEN, CAUSE);

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // GeolocationClient fallbacks
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GeolocationClient.misZonasFallback: devuelve lista vacía")
    void geolocation_misZonasFallback() {
        GeolocationClient client = new GeolocationClient(mock(RestClient.class));

        List<ZonaDto> result = client.misZonasFallback(TOKEN, CAUSE);

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // MatchingEngineClient fallbacks
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("MatchingEngineClient.buscarMatchesFallback: devuelve lista vacía")
    void matchingEngine_buscarMatchesFallback() {
        MatchingEngineClient client = new MatchingEngineClient(mock(RestClient.class));

        List<MatchDto> result = client.buscarMatchesFallback(1L, CAUSE);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("MatchingEngineClient.getMatchesPorReporteFallback: devuelve lista vacía")
    void matchingEngine_getMatchesPorReporteFallback() {
        MatchingEngineClient client = new MatchingEngineClient(mock(RestClient.class));

        List<MatchDto> result = client.getMatchesPorReporteFallback(1L, CAUSE);

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // NotificacionClient fallbacks
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("NotificacionClient.misNotificacionesFallback: devuelve lista vacía")
    void notification_misNotificacionesFallback() {
        NotificacionClient client = new NotificacionClient(mock(RestClient.class));

        List<NotificacionDto> result = client.misNotificacionesFallback(TOKEN, CAUSE);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("NotificacionClient.contarNoLeidasFallback: devuelve 0")
    void notification_contarNoLeidasFallback() {
        NotificacionClient client = new NotificacionClient(mock(RestClient.class));

        long result = client.contarNoLeidasFallback(TOKEN, CAUSE);

        assertThat(result).isZero();
    }
}
