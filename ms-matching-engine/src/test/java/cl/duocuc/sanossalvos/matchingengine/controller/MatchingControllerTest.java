package cl.duocuc.sanossalvos.matchingengine.controller;

import cl.duocuc.sanossalvos.matchingengine.dto.BuscarMatchesRequest;
import cl.duocuc.sanossalvos.matchingengine.dto.MatchResponse;
import cl.duocuc.sanossalvos.matchingengine.model.EstadoMatch;
import cl.duocuc.sanossalvos.matchingengine.service.MatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingControllerTest {

    @Mock MatchingService matchingService;
    @InjectMocks MatchingController controller;

    private MatchResponse buildMatch(Long id) {
        return MatchResponse.builder()
                .id(id).reportePerdidoId(1L).reporteEncontradoId(2L)
                .puntuacion(80).distanciaKm(BigDecimal.valueOf(3.5))
                .estado(EstadoMatch.PENDIENTE).fechaCreacion(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("buscarMatches: retorna 200 con lista de matches")
    void buscarMatches_returns200() {
        when(matchingService.buscarMatches(1L)).thenReturn(List.of(buildMatch(10L)));
        BuscarMatchesRequest req = new BuscarMatchesRequest();
        req.setReporteId(1L);

        ResponseEntity<List<MatchResponse>> resp = controller.buscarMatches(req);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        verify(matchingService).buscarMatches(1L);
    }

    @Test
    @DisplayName("obtenerMatches: retorna 200 con matches del reporte")
    void obtenerMatches_returns200() {
        when(matchingService.obtenerMatchesPorReporte(1L)).thenReturn(List.of(buildMatch(10L)));

        ResponseEntity<List<MatchResponse>> resp = controller.obtenerMatches(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        verify(matchingService).obtenerMatchesPorReporte(1L);
    }

    @Test
    @DisplayName("actualizarEstado: retorna 200 con match actualizado")
    void actualizarEstado_returns200() {
        MatchResponse confirmado = buildMatch(10L);
        confirmado.setEstado(EstadoMatch.CONFIRMADO);
        when(matchingService.actualizarEstado(10L, EstadoMatch.CONFIRMADO)).thenReturn(confirmado);

        ResponseEntity<MatchResponse> resp =
                controller.actualizarEstado(10L, Map.of("estado", "CONFIRMADO"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getEstado()).isEqualTo(EstadoMatch.CONFIRMADO);
    }
}
