package cl.duocuc.sanossalvos.bff.controller;

import cl.duocuc.sanossalvos.bff.dto.DashboardDto;
import cl.duocuc.sanossalvos.bff.dto.MapaDto;
import cl.duocuc.sanossalvos.bff.dto.ReporteDetalleDto;
import cl.duocuc.sanossalvos.bff.dto.ext.MatchDto;
import cl.duocuc.sanossalvos.bff.service.BffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BffControllerTest {

    @Mock BffService bffService;

    @InjectMocks BffController controller;

    private static final String TOKEN = "Bearer eyJtest";

    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken("user@test.cl", 1L, List.of());
        auth.setDetails(TOKEN);
    }

    @Test
    @DisplayName("getMapa: delega al servicio y devuelve 200 con MapaDto")
    void getMapa_returnsOk() {
        MapaDto mapaDto = MapaDto.builder().reportes(List.of()).zonas(List.of()).build();
        when(bffService.getMapaData(TOKEN)).thenReturn(mapaDto);

        ResponseEntity<MapaDto> resp = controller.getMapa(auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo(mapaDto);
        verify(bffService).getMapaData(TOKEN);
    }

    @Test
    @DisplayName("getDashboard: delega al servicio y devuelve 200 con DashboardDto")
    void getDashboard_returnsOk() {
        DashboardDto dashboardDto = DashboardDto.builder()
                .totalReportesPerdidos(2)
                .totalReportesEncontrados(1)
                .totalMatchesPendientes(3)
                .notificacionesNoLeidas(5L)
                .reportesRecientes(List.of())
                .matchesRecientes(List.of())
                .build();
        when(bffService.getDashboard(TOKEN)).thenReturn(dashboardDto);

        ResponseEntity<DashboardDto> resp = controller.getDashboard(auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTotalReportesPerdidos()).isEqualTo(2);
        verify(bffService).getDashboard(TOKEN);
    }

    @Test
    @DisplayName("getReporteDetalle: delega al servicio con el ID correcto y devuelve 200")
    void getReporteDetalle_returnsOk() {
        ReporteDetalleDto detalle = ReporteDetalleDto.builder()
                .reporte(null)
                .matches(List.of())
                .build();
        when(bffService.getReporteDetalle(10L, TOKEN)).thenReturn(detalle);

        ResponseEntity<ReporteDetalleDto> resp = controller.getReporteDetalle(10L, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bffService).getReporteDetalle(10L, TOKEN);
    }

    @Test
    @DisplayName("buscarMatches: dispara el motor de matching y devuelve la lista de matches")
    void buscarMatches_returnsMatchList() {
        MatchDto match = new MatchDto();
        match.setId(1L);
        match.setPuntuacion(80);
        match.setEstado("PENDIENTE");

        when(bffService.buscarMatches(5L)).thenReturn(List.of(match));

        ResponseEntity<List<MatchDto>> resp = controller.buscarMatches(5L, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        assertThat(resp.getBody().get(0).getEstado()).isEqualTo("PENDIENTE");
        verify(bffService).buscarMatches(5L);
    }
}
