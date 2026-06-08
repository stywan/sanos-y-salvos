package cl.duocuc.sanossalvos.petmanagement.controller;

import cl.duocuc.sanossalvos.petmanagement.dto.CambiarEstadoRequest;
import cl.duocuc.sanossalvos.petmanagement.dto.CrearReporteRequest;
import cl.duocuc.sanossalvos.petmanagement.dto.ReporteResponse;
import cl.duocuc.sanossalvos.petmanagement.model.EstadoReporte;
import cl.duocuc.sanossalvos.petmanagement.model.TipoReporte;
import cl.duocuc.sanossalvos.petmanagement.service.ReporteService;
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
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock ReporteService reporteService;
    @InjectMocks ReporteController controller;

    private Authentication auth;
    private ReporteResponse reporteResponse;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken("user@test.cl", 1L, List.of());
        reporteResponse = ReporteResponse.builder()
                .id(10L).usuarioId(1L).tipo(TipoReporte.PERDIDO)
                .estado(EstadoReporte.ACTIVO).nombreMascota("Rex").build();
    }

    @Test
    @DisplayName("crear: delega al servicio con usuarioId del token y retorna 201")
    void crear_returns201() {
        CrearReporteRequest req = new CrearReporteRequest();
        when(reporteService.crearReporte(eq(req), eq(1L))).thenReturn(reporteResponse);

        ResponseEntity<ReporteResponse> resp = controller.crear(req, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getId()).isEqualTo(10L);
        verify(reporteService).crearReporte(req, 1L);
    }

    @Test
    @DisplayName("listar: retorna 200 con lista de reportes (auth presente)")
    void listar_conAuth_returns200() {
        when(reporteService.listarReportes(null, null, 1L)).thenReturn(List.of(reporteResponse));

        ResponseEntity<List<ReporteResponse>> resp = controller.listar(null, null, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("listar: retorna 200 sin auth (endpoint público)")
    void listar_sinAuth_returns200() {
        when(reporteService.listarReportes(null, null, null)).thenReturn(List.of(reporteResponse));

        ResponseEntity<List<ReporteResponse>> resp = controller.listar(null, null, null);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("obtener: retorna 200 con el reporte solicitado")
    void obtener_returns200() {
        when(reporteService.obtenerReporte(10L, 1L)).thenReturn(reporteResponse);

        ResponseEntity<ReporteResponse> resp = controller.obtener(10L, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNombreMascota()).isEqualTo("Rex");
    }

    @Test
    @DisplayName("obtener: sin auth retorna 200 con usuarioId null (endpoint público)")
    void obtener_sinAuth_returns200() {
        when(reporteService.obtenerReporte(10L, null)).thenReturn(reporteResponse);

        ResponseEntity<ReporteResponse> resp = controller.obtener(10L, null);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("misReportes: retorna solo los reportes del usuario autenticado")
    void misReportes_returns200() {
        when(reporteService.listarPorUsuario(1L)).thenReturn(List.of(reporteResponse));

        ResponseEntity<List<ReporteResponse>> resp = controller.misReportes(auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("cambiarEstado: delega al servicio y retorna 200 con reporte actualizado")
    void cambiarEstado_returns200() {
        CambiarEstadoRequest req = new CambiarEstadoRequest();
        req.setEstado(EstadoReporte.RESUELTO);
        ReporteResponse resuelto = ReporteResponse.builder()
                .id(10L).estado(EstadoReporte.RESUELTO).build();
        when(reporteService.cambiarEstado(10L, req, 1L)).thenReturn(resuelto);

        ResponseEntity<ReporteResponse> resp = controller.cambiarEstado(10L, req, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getEstado()).isEqualTo(EstadoReporte.RESUELTO);
    }

    @Test
    @DisplayName("registrarAvistamiento: retorna 200 con reporte resuelto")
    void registrarAvistamiento_returns200() {
        ReporteResponse resuelto = ReporteResponse.builder()
                .id(10L).estado(EstadoReporte.RESUELTO).build();
        when(reporteService.registrarAvistamiento(10L, 1L)).thenReturn(resuelto);

        ResponseEntity<ReporteResponse> resp = controller.registrarAvistamiento(10L, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getEstado()).isEqualTo(EstadoReporte.RESUELTO);
    }
}
