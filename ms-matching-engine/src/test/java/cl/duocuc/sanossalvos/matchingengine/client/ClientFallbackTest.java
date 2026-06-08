package cl.duocuc.sanossalvos.matchingengine.client;

import cl.duocuc.sanossalvos.matchingengine.dto.ext.CrearNotificacionRequest;
import cl.duocuc.sanossalvos.matchingengine.dto.ext.FiltrarCercanosRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ClientFallbackTest {

    private PetManagementClient petClient;
    private GeolocationClient   geoClient;
    private NotificacionClient  notifClient;

    @BeforeEach
    void setUp() {
        RestClient restClient = mock(RestClient.class);
        petClient  = new PetManagementClient(restClient);
        geoClient  = new GeolocationClient(restClient);
        notifClient = new NotificacionClient(restClient);
    }

    // ── PetManagementClient ────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerReporteFallback: retorna Optional.empty()")
    void obtenerReporteFallback_returnsEmpty() {
        var result = petClient.obtenerReporteFallback(1L, new RuntimeException("CB open"));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listarReportesFallback: retorna lista vacía")
    void listarReportesFallback_returnsEmptyList() {
        var result = petClient.listarReportesFallback("PERDIDO", "ACTIVO", new RuntimeException("CB open"));
        assertThat(result).isEmpty();
    }

    // ── GeolocationClient ─────────────────────────────────────────────────

    @Test
    @DisplayName("filtrarCercanosFallback: retorna lista vacía")
    void filtrarCercanosFallback_returnsEmptyList() {
        var req = FiltrarCercanosRequest.builder().latitud(-33.45).longitud(-70.65).radioKm(5.0).build();
        var result = geoClient.filtrarCercanosFallback(req, new RuntimeException("CB open"));
        assertThat(result).isEmpty();
    }

    // ── NotificacionClient ────────────────────────────────────────────────

    @Test
    @DisplayName("crearNotificacionFallback: no lanza excepción")
    void crearNotificacionFallback_noLanzaExcepcion() {
        CrearNotificacionRequest req = CrearNotificacionRequest.builder()
                .usuarioId(10L).build();

        notifClient.crearNotificacionFallback(req, new RuntimeException("CB open"));
        // No excepción = OK
    }
}
