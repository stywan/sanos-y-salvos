package cl.duocuc.sanossalvos.geolocation.controller;

import cl.duocuc.sanossalvos.geolocation.dto.CrearZonaRequest;
import cl.duocuc.sanossalvos.geolocation.dto.ZonaResponse;
import cl.duocuc.sanossalvos.geolocation.service.ZonaBusquedaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geo/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final ZonaBusquedaService zonaService;

    /** Crear zona de búsqueda (requiere JWT) */
    @PostMapping
    public ResponseEntity<ZonaResponse> crearZona(@Valid @RequestBody CrearZonaRequest request,
                                                  Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaService.crearZona(request, usuarioId));
    }

    /** Listar mis zonas activas (requiere JWT) */
    @GetMapping("/mis-zonas")
    public ResponseEntity<List<ZonaResponse>> misZonas(Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        return ResponseEntity.ok(zonaService.misZonas(usuarioId));
    }

    /** Desactivar zona (solo el dueño, requiere JWT) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarZona(@PathVariable Long id, Authentication auth) {
        Long usuarioId = (Long) auth.getCredentials();
        zonaService.eliminarZona(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Zonas activas que contienen un punto geográfico.
     * Endpoint interno: lo llama el matching engine para saber a quién notificar.
     * No requiere JWT.
     */
    @GetMapping("/cercanas")
    public ResponseEntity<List<ZonaResponse>> zonasQueContienenPunto(
            @RequestParam double lat,
            @RequestParam double lon) {
        return ResponseEntity.ok(zonaService.zonasQueContienenPunto(lat, lon));
    }
}
