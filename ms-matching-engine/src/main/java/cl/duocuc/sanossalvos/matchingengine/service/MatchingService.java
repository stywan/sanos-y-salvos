package cl.duocuc.sanossalvos.matchingengine.service;

import cl.duocuc.sanossalvos.matchingengine.client.GeolocationClient;
import cl.duocuc.sanossalvos.matchingengine.client.NotificacionClient;
import cl.duocuc.sanossalvos.matchingengine.client.PetManagementClient;
import cl.duocuc.sanossalvos.matchingengine.dto.MatchResponse;
import cl.duocuc.sanossalvos.matchingengine.dto.ext.*;
import cl.duocuc.sanossalvos.matchingengine.exception.MatchNotFoundException;
import cl.duocuc.sanossalvos.matchingengine.model.EstadoMatch;
import cl.duocuc.sanossalvos.matchingengine.model.Match;
import cl.duocuc.sanossalvos.matchingengine.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private static final double RADIO_BUSQUEDA_KM = 20.0;

    private final PetManagementClient petClient;
    private final GeolocationClient   geoClient;
    private final NotificacionClient  notifClient;
    private final MatchRepository     matchRepository;
    private final PuntuacionService   puntuacionService;

    /**
     * Busca matches para el reporte dado.
     * Flujo:
     *  1. Obtener el reporte desde ms-pet-management
     *  2. Obtener candidatos del tipo contrario (PERDIDO↔ENCONTRADO)
     *  3. Filtrar por proximidad (≤20 km) vía ms-geolocation
     *  4. Puntuar cada candidato
     *  5. Persistir matches con puntuación ≥ MINIMA
     *  6. Notificar al usuario via ms-notification
     */
    @Transactional
    public List<MatchResponse> buscarMatches(Long reporteId) {
        // 1. Obtener reporte origen
        ReporteDto origen = petClient.obtenerReporte(reporteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reporte no encontrado o ms-pet-management no disponible: " + reporteId));

        if (!"ACTIVO".equals(origen.getEstado())) {
            log.info("Reporte {} no está ACTIVO, se omite el matching", reporteId);
            return List.of();
        }

        // 2. Obtener candidatos del tipo contrario
        String tipoCandidatos = "PERDIDO".equals(origen.getTipo()) ? "ENCONTRADO" : "PERDIDO";
        List<ReporteDto> candidatos = petClient.listarReportes(tipoCandidatos, "ACTIVO");

        if (candidatos.isEmpty()) {
            log.info("Sin candidatos {} activos para el reporte {}", tipoCandidatos, reporteId);
            return List.of();
        }

        // 3. Filtrar por proximidad via ms-geolocation
        List<FiltrarCercanosRequest.PuntoDto> puntos = candidatos.stream()
                .filter(c -> c.getLatitud() != null && c.getLongitud() != null)
                .map(c -> FiltrarCercanosRequest.PuntoDto.builder()
                        .id(c.getId())
                        .latitud(c.getLatitud())
                        .longitud(c.getLongitud())
                        .build())
                .toList();

        List<PuntoCercanoDto> cercanos = geoClient.filtrarCercanos(
                FiltrarCercanosRequest.builder()
                        .latitud(origen.getLatitud())
                        .longitud(origen.getLongitud())
                        .radioKm(RADIO_BUSQUEDA_KM)
                        .puntos(puntos)
                        .build()
        );

        // Mapa id → distanciaKm para lookup rápido
        Map<Long, Double> distanciasPorId = cercanos.stream()
                .collect(Collectors.toMap(PuntoCercanoDto::getId, PuntoCercanoDto::getDistanciaKm));

        // Mapa id → reporte candidato
        Map<Long, ReporteDto> candidatosPorId = candidatos.stream()
                .collect(Collectors.toMap(ReporteDto::getId, Function.identity()));

        // 4. Puntuar y filtrar
        List<Match> matches = cercanos.stream()
                .map(cercano -> {
                    ReporteDto candidato = candidatosPorId.get(cercano.getId());
                    if (candidato == null) return null;

                    ReporteDto perdido    = "PERDIDO".equals(origen.getTipo()) ? origen : candidato;
                    ReporteDto encontrado = "ENCONTRADO".equals(origen.getTipo()) ? origen : candidato;

                    int puntuacion = puntuacionService.calcular(
                            perdido, encontrado, distanciasPorId.getOrDefault(cercano.getId(), 999.0));

                    if (puntuacion < PuntuacionService.PUNTUACION_MINIMA) return null;

                    // 5. Evitar duplicados
                    return matchRepository
                            .findByReportePerdidoIdAndReporteEncontradoId(
                                    perdido.getId(), encontrado.getId())
                            .orElseGet(() -> Match.builder()
                                    .reportePerdidoId(perdido.getId())
                                    .reporteEncontradoId(encontrado.getId())
                                    .puntuacion(puntuacion)
                                    .distanciaKm(BigDecimal.valueOf(
                                            distanciasPorId.getOrDefault(cercano.getId(), 0.0)))
                                    .estado(EstadoMatch.PENDIENTE)
                                    .fechaCreacion(LocalDateTime.now())
                                    .build());
                })
                .filter(m -> m != null && m.getId() == null) // solo los nuevos
                .map(matchRepository::save)
                .toList();

        // 6. Notificar al dueño del reporte origen
        matches.forEach(m -> notificar(origen, m));

        log.info("Reporte {}: {} matches nuevos encontrados", reporteId, matches.size());
        return matches.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> obtenerMatchesPorReporte(Long reporteId) {
        // Busca en ambas columnas (puede ser perdido o encontrado)
        List<Match> perdidos   = matchRepository.findByReportePerdidoIdOrderByPuntuacionDesc(reporteId);
        List<Match> encontrados = matchRepository.findByReporteEncontradoIdOrderByPuntuacionDesc(reporteId);

        return java.util.stream.Stream.concat(perdidos.stream(), encontrados.stream())
                .distinct()
                .sorted((a, b) -> b.getPuntuacion() - a.getPuntuacion())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MatchResponse actualizarEstado(Long matchId, EstadoMatch nuevoEstado) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
        match.setEstado(nuevoEstado);
        return toResponse(matchRepository.save(match));
    }

    // ── Privados ─────────────────────────────────────────────────────────────

    private void notificar(ReporteDto origen, Match match) {
        try {
            notifClient.crearNotificacion(CrearNotificacionRequest.builder()
                    .usuarioId(origen.getUsuarioId())
                    .tipo("MATCH_ENCONTRADO")
                    .titulo("¡Posible mascota encontrada!")
                    .mensaje("Se encontró una coincidencia para tu reporte. " +
                             "Puntuación de similitud: " + match.getPuntuacion() + "/100.")
                    .reporteId("PERDIDO".equals(origen.getTipo())
                            ? match.getReportePerdidoId()
                            : match.getReporteEncontradoId())
                    .build());
        } catch (Exception e) {
            log.warn("No se pudo notificar al usuario {}: {}", origen.getUsuarioId(), e.getMessage());
        }
    }

    private MatchResponse toResponse(Match m) {
        return MatchResponse.builder()
                .id(m.getId())
                .reportePerdidoId(m.getReportePerdidoId())
                .reporteEncontradoId(m.getReporteEncontradoId())
                .puntuacion(m.getPuntuacion())
                .distanciaKm(m.getDistanciaKm())
                .estado(m.getEstado())
                .fechaCreacion(m.getFechaCreacion())
                .build();
    }
}
