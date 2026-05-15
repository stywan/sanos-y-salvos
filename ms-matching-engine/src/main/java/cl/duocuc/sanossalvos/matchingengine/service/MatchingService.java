package cl.duocuc.sanossalvos.matchingengine.service;

import cl.duocuc.sanossalvos.matchingengine.client.GeolocationClient;
import cl.duocuc.sanossalvos.matchingengine.client.NotificacionClient;
import cl.duocuc.sanossalvos.matchingengine.client.PetManagementClient;
import cl.duocuc.sanossalvos.matchingengine.dto.MatchResponse;
import cl.duocuc.sanossalvos.matchingengine.dto.ext.*;
import cl.duocuc.sanossalvos.matchingengine.exception.MatchNotFoundException;
import cl.duocuc.sanossalvos.matchingengine.kafka.MatchEventPublisher;
import cl.duocuc.sanossalvos.matchingengine.kafka.event.MatchEncontradoEvent;
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
    private final NotificacionClient  notifClient; // mantenido como fallback legacy
    private final MatchRepository     matchRepository;
    private final PuntuacionService   puntuacionService;
    private final MatchEventPublisher matchEventPublisher;

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

        // 6. Notificar a AMBOS dueños del match (origen y candidato)
        matches.forEach(m -> {
            Long candidatoId = "PERDIDO".equals(origen.getTipo())
                    ? m.getReporteEncontradoId()
                    : m.getReportePerdidoId();
            ReporteDto candidato = candidatosPorId.get(candidatoId);
            notificar(origen, candidato, m);
        });

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

    /**
     * Publica el evento match.encontrado en Kafka para AMBOS dueños del match.
     * Cada usuario recibe una notificación apuntando al reporte del OTRO lado
     * (perspectiva natural: "mira el reporte que coincide con el tuyo").
     * Si los dos reportes son del mismo usuario, se publica solo una vez.
     */
    private void notificar(ReporteDto origen, ReporteDto candidato, Match match) {
        Double distancia = match.getDistanciaKm() != null
                ? match.getDistanciaKm().doubleValue() : null;
        double score = match.getPuntuacion();
        LocalDateTime ahora = LocalDateTime.now();

        // 1. Notificar al dueño del ORIGEN → apunta al CANDIDATO
        matchEventPublisher.publicarMatchEncontrado(new MatchEncontradoEvent(
                origen.getId(),
                candidato != null ? candidato.getId() : null,
                origen.getUsuarioId(),
                origen.getEmailContacto(),
                origen.getNombreMascota(),
                distancia,
                score,
                ahora
        ));

        // 2. Notificar al dueño del CANDIDATO → apunta al ORIGEN
        //    (solo si es un usuario distinto, evita doble notif al mismo dueño)
        if (candidato != null
                && candidato.getUsuarioId() != null
                && !candidato.getUsuarioId().equals(origen.getUsuarioId())) {
            matchEventPublisher.publicarMatchEncontrado(new MatchEncontradoEvent(
                    candidato.getId(),
                    origen.getId(),
                    candidato.getUsuarioId(),
                    candidato.getEmailContacto(),
                    candidato.getNombreMascota(),
                    distancia,
                    score,
                    ahora
            ));
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
