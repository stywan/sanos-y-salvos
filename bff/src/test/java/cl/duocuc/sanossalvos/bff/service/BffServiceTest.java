package cl.duocuc.sanossalvos.bff.service;

import cl.duocuc.sanossalvos.bff.client.GeolocationClient;
import cl.duocuc.sanossalvos.bff.client.MatchingEngineClient;
import cl.duocuc.sanossalvos.bff.client.NotificacionClient;
import cl.duocuc.sanossalvos.bff.client.PetManagementClient;
import cl.duocuc.sanossalvos.bff.dto.DashboardDto;
import cl.duocuc.sanossalvos.bff.dto.MapaDto;
import cl.duocuc.sanossalvos.bff.dto.ReporteDetalleDto;
import cl.duocuc.sanossalvos.bff.dto.ext.MatchDto;
import cl.duocuc.sanossalvos.bff.dto.ext.ReporteDto;
import cl.duocuc.sanossalvos.bff.dto.ext.ZonaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BffServiceTest {

    @Mock PetManagementClient petManagementClient;
    @Mock GeolocationClient   geolocationClient;
    @Mock MatchingEngineClient matchingEngineClient;
    @Mock NotificacionClient   notificacionClient;

    @InjectMocks BffService bffService;

    private static final String TOKEN = "Bearer eyJtest";

    private ReporteDto reportePerdido;
    private ReporteDto reporteEncontrado;
    private ZonaDto    zona;
    private MatchDto   matchPendiente;
    private MatchDto   matchConfirmado;

    @BeforeEach
    void setUp() {
        reportePerdido = new ReporteDto();
        reportePerdido.setId(1L);
        reportePerdido.setTipo("PERDIDO");
        reportePerdido.setEstado("ACTIVO");

        reporteEncontrado = new ReporteDto();
        reporteEncontrado.setId(2L);
        reporteEncontrado.setTipo("ENCONTRADO");
        reporteEncontrado.setEstado("ACTIVO");

        zona = new ZonaDto();
        zona.setId(10L);
        zona.setNombre("Zona centro");
        zona.setLatitudCentro(BigDecimal.valueOf(-33.45));
        zona.setLongitudCentro(BigDecimal.valueOf(-70.65));
        zona.setRadioKm(BigDecimal.valueOf(5.0));

        matchPendiente = new MatchDto();
        matchPendiente.setId(100L);
        matchPendiente.setReportePerdidoId(1L);
        matchPendiente.setReporteEncontradoId(2L);
        matchPendiente.setPuntuacion(75);
        matchPendiente.setEstado("PENDIENTE");

        matchConfirmado = new MatchDto();
        matchConfirmado.setId(101L);
        matchConfirmado.setEstado("CONFIRMADO");
    }

    // -----------------------------------------------------------------------
    // getMapaData
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getMapaData: agrega reportes activos y zonas correctamente")
    void getMapaData_returnsAggregatedData() {
        when(petManagementClient.listarReportesActivos(TOKEN))
                .thenReturn(List.of(reportePerdido, reporteEncontrado));
        when(geolocationClient.misZonas(TOKEN))
                .thenReturn(List.of(zona));

        MapaDto resultado = bffService.getMapaData(TOKEN);

        assertThat(resultado.getReportes()).hasSize(2);
        assertThat(resultado.getZonas()).hasSize(1);
        assertThat(resultado.getReportes().get(0).getTipo()).isEqualTo("PERDIDO");
        verify(petManagementClient).listarReportesActivos(TOKEN);
        verify(geolocationClient).misZonas(TOKEN);
    }

    @Test
    @DisplayName("getMapaData: funciona correctamente cuando los servicios devuelven listas vacías")
    void getMapaData_emptyServices_returnsEmptyLists() {
        when(petManagementClient.listarReportesActivos(TOKEN)).thenReturn(List.of());
        when(geolocationClient.misZonas(TOKEN)).thenReturn(List.of());

        MapaDto resultado = bffService.getMapaData(TOKEN);

        assertThat(resultado.getReportes()).isEmpty();
        assertThat(resultado.getZonas()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // getDashboard
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getDashboard: agrega reportes, matches y noLeidas correctamente")
    void getDashboard_returnsAggregatedStats() {
        when(petManagementClient.misReportes(TOKEN))
                .thenReturn(List.of(reportePerdido, reporteEncontrado));
        when(matchingEngineClient.getMatchesPorReporte(1L))
                .thenReturn(List.of(matchPendiente, matchConfirmado));
        when(notificacionClient.contarNoLeidas(TOKEN)).thenReturn(3L);

        DashboardDto resultado = bffService.getDashboard(TOKEN);

        assertThat(resultado.getTotalReportesPerdidos()).isEqualTo(1);
        assertThat(resultado.getTotalReportesEncontrados()).isEqualTo(1);
        assertThat(resultado.getTotalMatchesPendientes()).isEqualTo(1);   // solo PENDIENTE
        assertThat(resultado.getNotificacionesNoLeidas()).isEqualTo(3L);
        assertThat(resultado.getReportesRecientes()).hasSize(2);          // límite 3
        assertThat(resultado.getMatchesRecientes()).hasSize(2);
    }

    @Test
    @DisplayName("getDashboard: sin reportes, no llama a matching-engine")
    void getDashboard_noReportes_noMatchingCall() {
        when(petManagementClient.misReportes(TOKEN)).thenReturn(List.of());
        when(notificacionClient.contarNoLeidas(TOKEN)).thenReturn(0L);

        DashboardDto resultado = bffService.getDashboard(TOKEN);

        assertThat(resultado.getTotalReportesPerdidos()).isZero();
        assertThat(resultado.getTotalReportesEncontrados()).isZero();
        assertThat(resultado.getTotalMatchesPendientes()).isZero();
        assertThat(resultado.getMatchesRecientes()).isEmpty();
        verify(matchingEngineClient, never()).getMatchesPorReporte(anyLong());
    }

    @Test
    @DisplayName("getDashboard: reportesRecientes limitado a 3 aunque haya más")
    void getDashboard_limitsRecentReportes() {
        ReporteDto r3 = new ReporteDto(); r3.setId(3L); r3.setTipo("PERDIDO");
        ReporteDto r4 = new ReporteDto(); r4.setId(4L); r4.setTipo("ENCONTRADO");
        ReporteDto r5 = new ReporteDto(); r5.setId(5L); r5.setTipo("PERDIDO");

        when(petManagementClient.misReportes(TOKEN))
                .thenReturn(List.of(reportePerdido, reporteEncontrado, r3, r4, r5));
        when(matchingEngineClient.getMatchesPorReporte(anyLong())).thenReturn(List.of());
        when(notificacionClient.contarNoLeidas(TOKEN)).thenReturn(0L);

        DashboardDto resultado = bffService.getDashboard(TOKEN);

        assertThat(resultado.getReportesRecientes()).hasSize(3);
        assertThat(resultado.getTotalReportesPerdidos()).isEqualTo(3);
        assertThat(resultado.getTotalReportesEncontrados()).isEqualTo(2);
    }

    // -----------------------------------------------------------------------
    // getReporteDetalle
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getReporteDetalle: combina reporte y matches")
    void getReporteDetalle_returnsCombinedData() {
        when(petManagementClient.obtenerReporte(1L, TOKEN))
                .thenReturn(Optional.of(reportePerdido));
        when(matchingEngineClient.getMatchesPorReporte(1L))
                .thenReturn(List.of(matchPendiente));

        ReporteDetalleDto resultado = bffService.getReporteDetalle(1L, TOKEN);

        assertThat(resultado.getReporte()).isEqualTo(reportePerdido);
        assertThat(resultado.getMatches()).hasSize(1);
        assertThat(resultado.getMatches().get(0).getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("getReporteDetalle: lanza excepción si el reporte no existe")
    void getReporteDetalle_reporteNotFound_throwsException() {
        when(petManagementClient.obtenerReporte(99L, TOKEN))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bffService.getReporteDetalle(99L, TOKEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getReporteDetalle: sin matches muestra lista vacía")
    void getReporteDetalle_noMatches_returnsEmptyList() {
        when(petManagementClient.obtenerReporte(1L, TOKEN))
                .thenReturn(Optional.of(reportePerdido));
        when(matchingEngineClient.getMatchesPorReporte(1L))
                .thenReturn(List.of());

        ReporteDetalleDto resultado = bffService.getReporteDetalle(1L, TOKEN);

        assertThat(resultado.getMatches()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // buscarMatches
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("buscarMatches: delega al motor de matching y devuelve resultados")
    void buscarMatches_returnsMatchingEngineResult() {
        when(matchingEngineClient.buscarMatches(1L))
                .thenReturn(List.of(matchPendiente));

        List<MatchDto> resultado = bffService.buscarMatches(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPuntuacion()).isEqualTo(75);
        verify(matchingEngineClient).buscarMatches(1L);
    }

    @Test
    @DisplayName("buscarMatches: devuelve lista vacía cuando no hay candidatos")
    void buscarMatches_noCandidates_returnsEmpty() {
        when(matchingEngineClient.buscarMatches(5L)).thenReturn(List.of());

        List<MatchDto> resultado = bffService.buscarMatches(5L);

        assertThat(resultado).isEmpty();
    }
}
