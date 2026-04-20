package cl.duocuc.sanossalvos.geolocation.service;

import cl.duocuc.sanossalvos.geolocation.dto.CrearZonaRequest;
import cl.duocuc.sanossalvos.geolocation.dto.ZonaResponse;
import cl.duocuc.sanossalvos.geolocation.exception.ZonaNotFoundException;
import cl.duocuc.sanossalvos.geolocation.model.ZonaBusqueda;
import cl.duocuc.sanossalvos.geolocation.repository.ZonaBusquedaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonaBusquedaService {

    private final ZonaBusquedaRepository zonaRepository;
    private final ProximidadService proximidadService;

    @Transactional
    public ZonaResponse crearZona(CrearZonaRequest request, Long usuarioId) {
        ZonaBusqueda zona = ZonaBusqueda.builder()
                .usuarioId(usuarioId)
                .nombre(request.getNombre())
                .latitudCentro(request.getLatitudCentro())
                .longitudCentro(request.getLongitudCentro())
                .radioKm(request.getRadioKm())
                .activa(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        return toResponse(zonaRepository.save(zona));
    }

    @Transactional(readOnly = true)
    public List<ZonaResponse> misZonas(Long usuarioId) {
        return zonaRepository.findByUsuarioIdAndActivaTrue(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void eliminarZona(Long id, Long usuarioId) {
        ZonaBusqueda zona = zonaRepository.findById(id)
                .orElseThrow(() -> new ZonaNotFoundException(id));

        if (!zona.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permiso para eliminar esta zona");
        }

        zona.setActiva(false);
        zonaRepository.save(zona);
    }

    /**
     * Devuelve las zonas activas cuyo radio contiene el punto dado.
     * Usado internamente por el matching engine para saber a qué usuarios notificar.
     */
    @Transactional(readOnly = true)
    public List<ZonaResponse> zonasQueContienenPunto(double latitud, double longitud) {
        return zonaRepository.findByActivaTrue().stream()
                .filter(z -> {
                    double dist = proximidadService.calcularDistanciaKm(
                            latitud, longitud,
                            z.getLatitudCentro().doubleValue(),
                            z.getLongitudCentro().doubleValue()
                    );
                    return dist <= z.getRadioKm().doubleValue();
                })
                .map(this::toResponse)
                .toList();
    }

    // ── Mapper ─────────────────────────────────────────────────────────────

    private ZonaResponse toResponse(ZonaBusqueda z) {
        return ZonaResponse.builder()
                .id(z.getId())
                .usuarioId(z.getUsuarioId())
                .nombre(z.getNombre())
                .latitudCentro(z.getLatitudCentro())
                .longitudCentro(z.getLongitudCentro())
                .radioKm(z.getRadioKm())
                .activa(z.getActiva())
                .fechaCreacion(z.getFechaCreacion())
                .build();
    }
}
