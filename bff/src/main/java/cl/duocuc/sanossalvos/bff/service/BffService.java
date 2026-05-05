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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BffService {

    private final PetManagementClient petManagementClient;
    private final GeolocationClient geolocationClient;
    private final MatchingEngineClient matchingEngineClient;
    private final NotificacionClient notificacionClient;

    // -----------------------------------------------------------------------
    // Mapa interactivo
    // -----------------------------------------------------------------------

    /**
     * Agrega reportes activos + zonas del usuario para poblar el mapa.
     *
     * @param token Token JWT completo ("Bearer eyJ...")
     */
    public MapaDto getMapaData(String token) {
        log.debug("BFF: getMapaData — agregando reportes activos y zonas");

        List<ReporteDto> reportes = petManagementClient.listarReportesActivos(token);
        var zonas = geolocationClient.misZonas(token);

        return MapaDto.builder()
                .reportes(reportes)
                .zonas(zonas)
                .build();
    }

    // -----------------------------------------------------------------------
    // Dashboard del usuario
    // -----------------------------------------------------------------------

    /**
     * Agrega estadísticas del usuario: reportes propios, matches pendientes y notificaciones no leídas.
     *
     * @param token Token JWT completo ("Bearer eyJ...")
     */
    public DashboardDto getDashboard(String token) {
        log.debug("BFF: getDashboard — agregando stats del usuario");

        // 1. Mis reportes (ms-pet-management)
        List<ReporteDto> misReportes = petManagementClient.misReportes(token);

        long perdidos    = misReportes.stream().filter(r -> "PERDIDO".equals(r.getTipo())).count();
        long encontrados = misReportes.stream().filter(r -> "ENCONTRADO".equals(r.getTipo())).count();

        // 2. Matches del reporte más reciente (ms-matching-engine)
        List<MatchDto> matchesRecientes = misReportes.stream()
                .findFirst()
                .map(r -> matchingEngineClient.getMatchesPorReporte(r.getId()))
                .orElse(List.of());

        long matchesPendientes = matchesRecientes.stream()
                .filter(m -> "PENDIENTE".equals(m.getEstado()))
                .count();

        // 3. Notificaciones no leídas (ms-notification)
        long noLeidas = notificacionClient.contarNoLeidas(token);

        return DashboardDto.builder()
                .totalReportesPerdidos((int) perdidos)
                .totalReportesEncontrados((int) encontrados)
                .totalMatchesPendientes((int) matchesPendientes)
                .notificacionesNoLeidas(noLeidas)
                .reportesRecientes(misReportes.stream().limit(3).collect(Collectors.toList()))
                .matchesRecientes(matchesRecientes)
                .build();
    }

    // -----------------------------------------------------------------------
    // Detalle de reporte con sus matches
    // -----------------------------------------------------------------------

    /**
     * Combina el detalle de un reporte con todos sus matches.
     *
     * @param reporteId ID del reporte
     * @param token     Token JWT completo ("Bearer eyJ...")
     */
    public ReporteDetalleDto getReporteDetalle(Long reporteId, String token) {
        log.debug("BFF: getReporteDetalle — reporte={}", reporteId);

        ReporteDto reporte = petManagementClient.obtenerReporte(reporteId, token)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reporte " + reporteId + " no encontrado"));

        List<MatchDto> matches = matchingEngineClient.getMatchesPorReporte(reporteId);

        return ReporteDetalleDto.builder()
                .reporte(reporte)
                .matches(matches)
                .build();
    }

    // -----------------------------------------------------------------------
    // Disparar búsqueda de matches
    // -----------------------------------------------------------------------

    /**
     * Solicita al motor de matching que busque candidatos para el reporte indicado.
     *
     * @param reporteId ID del reporte
     */
    public List<MatchDto> buscarMatches(Long reporteId) {
        log.debug("BFF: buscarMatches — reporte={}", reporteId);
        return matchingEngineClient.buscarMatches(reporteId);
    }
}
