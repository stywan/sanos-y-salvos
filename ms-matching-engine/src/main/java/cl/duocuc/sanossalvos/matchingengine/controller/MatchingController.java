package cl.duocuc.sanossalvos.matchingengine.controller;

import cl.duocuc.sanossalvos.matchingengine.dto.BuscarMatchesRequest;
import cl.duocuc.sanossalvos.matchingengine.dto.MatchResponse;
import cl.duocuc.sanossalvos.matchingengine.model.EstadoMatch;
import cl.duocuc.sanossalvos.matchingengine.service.MatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * Inicia la búsqueda de matches para un reporte.
     * Llamado por el BFF o frontend justo después de crear un reporte.
     * Sin autenticación: es una llamada interna del sistema.
     */
    @PostMapping("/buscar")
    public ResponseEntity<List<MatchResponse>> buscarMatches(
            @Valid @RequestBody BuscarMatchesRequest request) {
        return ResponseEntity.ok(matchingService.buscarMatches(request.getReporteId()));
    }

    /**
     * Obtiene todos los matches de un reporte (ya sea PERDIDO o ENCONTRADO).
     * Público: el dueño del reporte y cualquier usuario puede ver los matches.
     */
    @GetMapping("/matches")
    public ResponseEntity<List<MatchResponse>> obtenerMatches(
            @RequestParam Long reporteId) {
        return ResponseEntity.ok(matchingService.obtenerMatchesPorReporte(reporteId));
    }

    /**
     * Actualiza el estado de un match (CONFIRMADO / DESCARTADO).
     * En una versión futura esto requeriría JWT, por ahora es interno.
     */
    @PatchMapping("/matches/{id}/estado")
    public ResponseEntity<MatchResponse> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        EstadoMatch estado = EstadoMatch.valueOf(body.get("estado"));
        return ResponseEntity.ok(matchingService.actualizarEstado(id, estado));
    }
}
