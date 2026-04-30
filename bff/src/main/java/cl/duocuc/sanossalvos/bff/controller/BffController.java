package cl.duocuc.sanossalvos.bff.controller;

import cl.duocuc.sanossalvos.bff.dto.DashboardDto;
import cl.duocuc.sanossalvos.bff.dto.MapaDto;
import cl.duocuc.sanossalvos.bff.dto.ReporteDetalleDto;
import cl.duocuc.sanossalvos.bff.dto.ext.MatchDto;
import cl.duocuc.sanossalvos.bff.service.BffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bff")
@RequiredArgsConstructor
public class BffController {

    private final BffService bffService;

    // -----------------------------------------------------------------------
    // Mapa interactivo
    // -----------------------------------------------------------------------

    /**
     * GET /bff/mapa
     * <p>
     * Respuesta agregada para la vista del mapa: reportes activos (PERDIDO y ENCONTRADO)
     * + zonas de búsqueda del usuario autenticado.
     */
    @GetMapping("/mapa")
    public ResponseEntity<MapaDto> getMapa(Authentication auth) {
        String token = (String) auth.getDetails();
        return ResponseEntity.ok(bffService.getMapaData(token));
    }

    // -----------------------------------------------------------------------
    // Dashboard
    // -----------------------------------------------------------------------

    /**
     * GET /bff/dashboard
     * <p>
     * Estadísticas del usuario: conteo de reportes, matches pendientes
     * y notificaciones no leídas.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard(Authentication auth) {
        String token = (String) auth.getDetails();
        return ResponseEntity.ok(bffService.getDashboard(token));
    }

    // -----------------------------------------------------------------------
    // Detalle de reporte
    // -----------------------------------------------------------------------

    /**
     * GET /bff/reportes/{id}/detalle
     * <p>
     * Detalle completo de un reporte junto con todos sus matches calculados.
     */
    @GetMapping("/reportes/{id}/detalle")
    public ResponseEntity<ReporteDetalleDto> getReporteDetalle(
            @PathVariable Long id,
            Authentication auth) {
        String token = (String) auth.getDetails();
        return ResponseEntity.ok(bffService.getReporteDetalle(id, token));
    }

    // -----------------------------------------------------------------------
    // Disparar matching
    // -----------------------------------------------------------------------

    /**
     * POST /bff/reportes/{id}/buscar-matches
     * <p>
     * Pide al motor de matching que calcule candidatos para el reporte indicado.
     * Devuelve la lista de matches encontrados (puede estar vacía si no hay candidatos).
     */
    @PostMapping("/reportes/{id}/buscar-matches")
    public ResponseEntity<List<MatchDto>> buscarMatches(
            @PathVariable Long id,
            Authentication auth) {
        // Verificamos que el usuario está autenticado (token ya validado por el filtro)
        return ResponseEntity.ok(bffService.buscarMatches(id));
    }
}
