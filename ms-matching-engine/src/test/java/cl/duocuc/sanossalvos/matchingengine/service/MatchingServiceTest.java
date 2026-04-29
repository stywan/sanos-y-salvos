package cl.duocuc.sanossalvos.matchingengine.service;

import cl.duocuc.sanossalvos.matchingengine.client.GeolocationClient;
import cl.duocuc.sanossalvos.matchingengine.client.NotificacionClient;
import cl.duocuc.sanossalvos.matchingengine.client.PetManagementClient;
import cl.duocuc.sanossalvos.matchingengine.dto.MatchResponse;
import cl.duocuc.sanossalvos.matchingengine.dto.ext.PuntoCercanoDto;
import cl.duocuc.sanossalvos.matchingengine.dto.ext.ReporteDto;
import cl.duocuc.sanossalvos.matchingengine.model.EstadoMatch;
import cl.duocuc.sanossalvos.matchingengine.model.Match;
import cl.duocuc.sanossalvos.matchingengine.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock PetManagementClient petClient;
    @Mock GeolocationClient   geoClient;
    @Mock NotificacionClient  notifClient;
    @Mock MatchRepository     matchRepository;
    @Mock PuntuacionService   puntuacionService;
    @InjectMocks MatchingService service;

    private ReporteDto reportePerdido;
    private ReporteDto reporteEncontrado;

    @BeforeEach
    void setUp() {
        reportePerdido = new ReporteDto();
        reportePerdido.setId(1L);
        reportePerdido.setUsuarioId(10L);
        reportePerdido.setTipo("PERDIDO");
        reportePerdido.setEstado("ACTIVO");
        reportePerdido.setEspecie("Perro");
        reportePerdido.setGenero("MACHO");
        reportePerdido.setColores(List.of("Negro", "Blanco"));
        reportePerdido.setLatitud(-33.45);
        reportePerdido.setLongitud(-70.65);

        reporteEncontrado = new ReporteDto();
        reporteEncontrado.setId(2L);
        reporteEncontrado.setUsuarioId(20L);
        reporteEncontrado.setTipo("ENCONTRADO");
        reporteEncontrado.setEstado("ACTIVO");
        reporteEncontrado.setEspecie("Perro");
        reporteEncontrado.setGenero("MACHO");
        reporteEncontrado.setColores(List.of("Negro", "Blanco"));
        reporteEncontrado.setLatitud(-33.46);
        reporteEncontrado.setLongitud(-70.66);
    }

    @Test
    void buscarMatches_encuentraMatchConPuntuacionSuficiente() {
        when(petClient.obtenerReporte(1L)).thenReturn(Optional.of(reportePerdido));
        when(petClient.listarReportes("ENCONTRADO", "ACTIVO")).thenReturn(List.of(reporteEncontrado));

        PuntoCercanoDto cercano = new PuntoCercanoDto();
        cercano.setId(2L);
        cercano.setLatitud(-33.46);
        cercano.setLongitud(-70.66);
        cercano.setDistanciaKm(1.5);

        when(geoClient.filtrarCercanos(any())).thenReturn(List.of(cercano));
        when(puntuacionService.calcular(any(), any(), anyDouble())).thenReturn(80);
        when(matchRepository.findByReportePerdidoIdAndReporteEncontradoId(1L, 2L))
                .thenReturn(Optional.empty());

        Match matchGuardado = Match.builder()
                .id(100L).reportePerdidoId(1L).reporteEncontradoId(2L)
                .puntuacion(80).distanciaKm(BigDecimal.valueOf(1.5))
                .estado(EstadoMatch.PENDIENTE).fechaCreacion(LocalDateTime.now()).build();
        when(matchRepository.save(any())).thenReturn(matchGuardado);

        List<MatchResponse> resultado = service.buscarMatches(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPuntuacion()).isEqualTo(80);
        verify(notifClient).crearNotificacion(any());
    }

    @Test
    void buscarMatches_puntuacionBaja_noGuardaMatch() {
        when(petClient.obtenerReporte(1L)).thenReturn(Optional.of(reportePerdido));
        when(petClient.listarReportes("ENCONTRADO", "ACTIVO")).thenReturn(List.of(reporteEncontrado));

        PuntoCercanoDto cercano = new PuntoCercanoDto();
        cercano.setId(2L); cercano.setLatitud(-33.46);
        cercano.setLongitud(-70.66); cercano.setDistanciaKm(1.5);
        when(geoClient.filtrarCercanos(any())).thenReturn(List.of(cercano));
        when(puntuacionService.calcular(any(), any(), anyDouble())).thenReturn(10); // < MINIMA

        List<MatchResponse> resultado = service.buscarMatches(1L);

        assertThat(resultado).isEmpty();
        verify(matchRepository, never()).save(any());
    }

    @Test
    void buscarMatches_reporteNoActivo_retornaVacio() {
        reportePerdido.setEstado("RESUELTO");
        when(petClient.obtenerReporte(1L)).thenReturn(Optional.of(reportePerdido));

        List<MatchResponse> resultado = service.buscarMatches(1L);

        assertThat(resultado).isEmpty();
        verifyNoInteractions(geoClient, notifClient);
    }

    @Test
    void buscarMatches_sinCandidatos_retornaVacio() {
        when(petClient.obtenerReporte(1L)).thenReturn(Optional.of(reportePerdido));
        when(petClient.listarReportes("ENCONTRADO", "ACTIVO")).thenReturn(List.of());

        assertThat(service.buscarMatches(1L)).isEmpty();
    }

    @Test
    void buscarMatches_petManagementNoDisponible_lanzaException() {
        when(petClient.obtenerReporte(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarMatches(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarEstado_cambiaCorrectamente() {
        Match match = Match.builder().id(1L).reportePerdidoId(1L).reporteEncontradoId(2L)
                .puntuacion(80).estado(EstadoMatch.PENDIENTE).fechaCreacion(LocalDateTime.now()).build();
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.save(any())).thenReturn(match);

        MatchResponse resp = service.actualizarEstado(1L, EstadoMatch.CONFIRMADO);
        assertThat(match.getEstado()).isEqualTo(EstadoMatch.CONFIRMADO);
    }
}
