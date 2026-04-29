package cl.duocuc.sanossalvos.geolocation.controller;

import cl.duocuc.sanossalvos.geolocation.dto.FiltrarCercanosRequest;
import cl.duocuc.sanossalvos.geolocation.dto.PuntoCercanoDto;
import cl.duocuc.sanossalvos.geolocation.service.ProximidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de cálculo de proximidad.
 * Sin autenticación: son llamados por otros microservicios (BFF, matching engine).
 */
@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class ProximidadController {

    private final ProximidadService proximidadService;

    /**
     * Filtra puntos (reportes) que están dentro de un radio dado.
     * Devuelve solo los que cumplen el criterio, ordenados de más cercano a más lejano.
     */
    @PostMapping("/filtrar-cercanos")
    public ResponseEntity<List<PuntoCercanoDto>> filtrarCercanos(
            @Valid @RequestBody FiltrarCercanosRequest request) {
        return ResponseEntity.ok(proximidadService.filtrarCercanos(request));
    }
}
