package cl.duocuc.sanossalvos.petmanagement.service;

import cl.duocuc.sanossalvos.petmanagement.dto.CambiarEstadoRequest;
import cl.duocuc.sanossalvos.petmanagement.dto.CrearReporteRequest;
import cl.duocuc.sanossalvos.petmanagement.dto.ReporteResponse;
import cl.duocuc.sanossalvos.petmanagement.exception.ReporteNotFoundException;
import cl.duocuc.sanossalvos.petmanagement.kafka.ReporteEventPublisher;
import cl.duocuc.sanossalvos.petmanagement.model.*;
import cl.duocuc.sanossalvos.petmanagement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock private ReporteRepository reporteRepository;
    @Mock private EspecieRepository especieRepository;
    @Mock private RazaRepository razaRepository;
    @Mock private ColorRepository colorRepository;
    @Mock private ReporteEventPublisher eventPublisher;

    @InjectMocks
    private ReporteService reporteService;

    private Especie especiePerro;
    private Raza razaBeagle;

    @BeforeEach
    void setUp() {
        especiePerro = Especie.builder().id(1L).nombre("Perro").build();
        razaBeagle   = Raza.builder().id(1L).nombre("Beagle").especie(especiePerro).build();
    }

    // ── Crear reporte ──────────────────────────────────────────────────────

    @Test
    void crearReporte_perdido_sinRaza_retornaResponse() {
        CrearReporteRequest req = buildRequest(TipoReporte.PERDIDO, null);

        when(especieRepository.findById(1L)).thenReturn(Optional.of(especiePerro));
        when(colorRepository.findByIdIn(anyList())).thenReturn(List.of());
        when(reporteRepository.save(any())).thenAnswer(inv -> {
            Reporte r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReporteResponse resp = reporteService.crearReporte(req, 10L);

        assertThat(resp.getTipo()).isEqualTo(TipoReporte.PERDIDO);
        assertThat(resp.getEstado()).isEqualTo(EstadoReporte.ACTIVO);
        assertThat(resp.getUsuarioId()).isEqualTo(10L);
        assertThat(resp.getEspecie()).isEqualTo("Perro");
    }

    @Test
    void crearReporte_encontrado_conRaza_retornaResponse() {
        CrearReporteRequest req = buildRequest(TipoReporte.ENCONTRADO, 1L);

        when(especieRepository.findById(1L)).thenReturn(Optional.of(especiePerro));
        when(razaRepository.findById(1L)).thenReturn(Optional.of(razaBeagle));
        when(colorRepository.findByIdIn(anyList())).thenReturn(List.of());
        when(reporteRepository.save(any())).thenAnswer(inv -> {
            Reporte r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });

        ReporteResponse resp = reporteService.crearReporte(req, 10L);

        assertThat(resp.getTipo()).isEqualTo(TipoReporte.ENCONTRADO);
        assertThat(resp.getRaza()).isEqualTo("Beagle");
    }

    @Test
    void crearReporte_especieInexistente_lanzaExcepcion() {
        CrearReporteRequest req = buildRequest(TipoReporte.PERDIDO, null);
        when(especieRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reporteService.crearReporte(req, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Especie no encontrada");
    }

    @Test
    void crearReporte_razaInexistente_lanzaExcepcion() {
        CrearReporteRequest req = buildRequest(TipoReporte.PERDIDO, 99L);
        when(especieRepository.findById(1L)).thenReturn(Optional.of(especiePerro));
        when(razaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reporteService.crearReporte(req, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Raza no encontrada");
    }

    // ── Obtener reporte ────────────────────────────────────────────────────

    @Test
    void obtenerReporte_existente_retornaResponse() {
        Reporte reporte = buildReporte(1L, 10L);
        when(reporteRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(reporte));

        ReporteResponse resp = reporteService.obtenerReporte(1L, 10L);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getTelefonoContacto()).isEqualTo("+56912345678"); // dueño ve su teléfono
    }

    @Test
    void obtenerReporte_inexistente_lanzaExcepcion() {
        when(reporteRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reporteService.obtenerReporte(99L, 10L))
                .isInstanceOf(ReporteNotFoundException.class);
    }

    @Test
    void obtenerReporte_telefonoOculto_usuarioExterno_noVeTelefono() {
        Reporte reporte = buildReporte(1L, 10L);
        reporte.setTelefonoVisible(false);
        when(reporteRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(reporte));

        ReporteResponse resp = reporteService.obtenerReporte(1L, 99L); // otro usuario

        assertThat(resp.getTelefonoContacto()).isNull();
    }

    // ── Cambiar estado ─────────────────────────────────────────────────────

    @Test
    void cambiarEstado_propietario_actualizaEstado() {
        Reporte reporte = buildReporte(1L, 10L);
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(any())).thenReturn(reporte);

        CambiarEstadoRequest req = new CambiarEstadoRequest();
        req.setEstado(EstadoReporte.RESUELTO);

        ReporteResponse resp = reporteService.cambiarEstado(1L, req, 10L);

        assertThat(resp.getEstado()).isEqualTo(EstadoReporte.RESUELTO);
    }

    @Test
    void cambiarEstado_noPropietario_lanzaExcepcion() {
        Reporte reporte = buildReporte(1L, 10L);
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));

        CambiarEstadoRequest req = new CambiarEstadoRequest();
        req.setEstado(EstadoReporte.RESUELTO);

        assertThatThrownBy(() -> reporteService.cambiarEstado(1L, req, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permiso");
    }

    // ── Listar reportes ────────────────────────────────────────────────────

    @Test
    void listarReportes_sinFiltros_retornaTodasLasEntradas() {
        when(reporteRepository.findAll()).thenReturn(List.of(buildReporte(1L, 10L)));

        List<ReporteResponse> resp = reporteService.listarReportes(null, null, null);

        assertThat(resp).hasSize(1);
        verify(reporteRepository).findAll();
    }

    @Test
    void listarReportes_porTipoYEstado_llamaMetodoCorrecto() {
        when(reporteRepository.findByTipoAndEstado(TipoReporte.PERDIDO, EstadoReporte.ACTIVO))
                .thenReturn(List.of(buildReporte(2L, 10L)));

        List<ReporteResponse> resp = reporteService.listarReportes(TipoReporte.PERDIDO, EstadoReporte.ACTIVO, null);

        assertThat(resp).hasSize(1);
        verify(reporteRepository).findByTipoAndEstado(TipoReporte.PERDIDO, EstadoReporte.ACTIVO);
    }

    @Test
    void listarReportes_soloPorTipo_llamaFindByTipo() {
        when(reporteRepository.findByTipo(TipoReporte.PERDIDO)).thenReturn(List.of());

        List<ReporteResponse> resp = reporteService.listarReportes(TipoReporte.PERDIDO, null, 1L);

        assertThat(resp).isEmpty();
        verify(reporteRepository).findByTipo(TipoReporte.PERDIDO);
    }

    @Test
    void listarReportes_soloPorEstado_llamaFindByEstado() {
        when(reporteRepository.findByEstado(EstadoReporte.ACTIVO)).thenReturn(List.of());

        List<ReporteResponse> resp = reporteService.listarReportes(null, EstadoReporte.ACTIVO, 1L);

        assertThat(resp).isEmpty();
        verify(reporteRepository).findByEstado(EstadoReporte.ACTIVO);
    }

    // ── Listar por usuario ─────────────────────────────────────────────────

    @Test
    void listarPorUsuario_retornaReportesDelUsuario() {
        when(reporteRepository.findByUsuarioId(10L)).thenReturn(List.of(buildReporte(1L, 10L)));

        List<ReporteResponse> resp = reporteService.listarPorUsuario(10L);

        assertThat(resp).hasSize(1);
        assertThat(resp.get(0).getUsuarioId()).isEqualTo(10L);
    }

    // ── Registrar avistamiento ─────────────────────────────────────────────

    @Test
    void registrarAvistamiento_existente_marcaResuelto() {
        Reporte r = buildReporte(1L, 10L);
        when(reporteRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(r));
        when(reporteRepository.save(any())).thenReturn(r);

        ReporteResponse resp = reporteService.registrarAvistamiento(1L, 99L);

        assertThat(resp.getEstado()).isEqualTo(EstadoReporte.RESUELTO);
        verify(eventPublisher).publicarAvistamiento(any(), eq(99L));
    }

    @Test
    void registrarAvistamiento_noExistente_lanzaExcepcion() {
        when(reporteRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reporteService.registrarAvistamiento(99L, 10L))
                .isInstanceOf(ReporteNotFoundException.class);
    }

    @Test
    void cambiarEstado_aActivo_noPublicaEventoResuelto() {
        Reporte reporte = buildReporte(1L, 10L);
        reporte.setEstado(EstadoReporte.RESUELTO);
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(any())).thenReturn(reporte);

        CambiarEstadoRequest req = new CambiarEstadoRequest();
        req.setEstado(EstadoReporte.ACTIVO);

        ReporteResponse resp = reporteService.cambiarEstado(1L, req, 10L);

        assertThat(resp.getEstado()).isEqualTo(EstadoReporte.ACTIVO);
        verify(eventPublisher, never()).publicarReporteResuelto(any());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private CrearReporteRequest buildRequest(TipoReporte tipo, Long razaId) {
        CrearReporteRequest req = new CrearReporteRequest();
        req.setTipo(tipo);
        req.setEspecieId(1L);
        req.setRazaId(razaId);
        req.setNombreMascota("Max");
        req.setGenero(GeneroMascota.MACHO);
        req.setFechaSuceso(LocalDate.now());
        req.setNombreContacto("Juan Pérez");
        req.setTelefonoContacto("+56912345678");
        req.setEmailContacto("juan@ejemplo.com");
        req.setTelefonoVisible(true);
        return req;
    }

    private Reporte buildReporte(Long id, Long usuarioId) {
        Mascota mascota = Mascota.builder()
                .id(1L).nombre("Max")
                .especie(especiePerro).raza(razaBeagle)
                .genero(GeneroMascota.MACHO)
                .build();

        return Reporte.builder()
                .id(id).usuarioId(usuarioId)
                .mascota(mascota)
                .tipo(TipoReporte.PERDIDO)
                .estado(EstadoReporte.ACTIVO)
                .fechaSuceso(LocalDate.now())
                .nombreContacto("Juan Pérez")
                .telefonoContacto("+56912345678")
                .emailContacto("juan@ejemplo.com")
                .telefonoVisible(true)
                .build();
    }
}
