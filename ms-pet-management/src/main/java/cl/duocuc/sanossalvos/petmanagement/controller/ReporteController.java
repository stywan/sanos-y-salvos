package cl.duocuc.sanossalvos.petmanagement.controller;

import cl.duocuc.sanossalvos.petmanagement.dto.CambiarEstadoRequest;
import cl.duocuc.sanossalvos.petmanagement.dto.CrearReporteRequest;
import cl.duocuc.sanossalvos.petmanagement.dto.ReporteResponse;
import cl.duocuc.sanossalvos.petmanagement.model.EstadoReporte;
import cl.duocuc.sanossalvos.petmanagement.model.TipoReporte;
import cl.duocuc.sanossalvos.petmanagement.service.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    /** Crea un nuevo reporte (requiere autenticación) */
    @PostMapping
    public ResponseEntity<ReporteResponse> crear(
            @Valid @RequestBody CrearReporteRequest request,
            Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reporteService.crearReporte(request, usuarioId));
    }

    /** Lista reportes con filtros opcionales (público) */
    @GetMapping
    public ResponseEntity<List<ReporteResponse>> listar(
            @RequestParam(required = false) TipoReporte tipo,
            @RequestParam(required = false) EstadoReporte estado,
            Authentication auth) {
        Long usuarioId = auth != null ? (Long) auth.getCredentials() : null;
        return ResponseEntity.ok(reporteService.listarReportes(tipo, estado, usuarioId));
    }

    /** Detalle de un reporte (público) */
    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponse> obtener(
            @PathVariable Long id,
            Authentication auth) {
        Long usuarioId = auth != null ? (Long) auth.getCredentials() : null;
        return ResponseEntity.ok(reporteService.obtenerReporte(id, usuarioId));
    }

    /** Reportes del usuario autenticado */
    @GetMapping("/mis-reportes")
    public ResponseEntity<List<ReporteResponse>> misReportes(Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        return ResponseEntity.ok(reporteService.listarPorUsuario(usuarioId));
    }

    /** Cambia el estado de un reporte (solo el dueño) */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ReporteResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request,
            Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        return ResponseEntity.ok(reporteService.cambiarEstado(id, request, usuarioId));
    }
}
