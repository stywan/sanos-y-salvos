package cl.duocuc.sanossalvos.petmanagement.service;

import cl.duocuc.sanossalvos.petmanagement.dto.CambiarEstadoRequest;
import cl.duocuc.sanossalvos.petmanagement.dto.CrearReporteRequest;
import cl.duocuc.sanossalvos.petmanagement.dto.ReporteResponse;
import cl.duocuc.sanossalvos.petmanagement.exception.ReporteNotFoundException;
import cl.duocuc.sanossalvos.petmanagement.model.*;
import cl.duocuc.sanossalvos.petmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final EspecieRepository especieRepository;
    private final RazaRepository razaRepository;
    private final ColorRepository colorRepository;

    @Transactional
    public ReporteResponse crearReporte(CrearReporteRequest request, Long usuarioId) {
        Especie especie = especieRepository.findById(request.getEspecieId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Especie no encontrada: " + request.getEspecieId()));

        Raza raza = null;
        if (request.getRazaId() != null) {
            raza = razaRepository.findById(request.getRazaId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Raza no encontrada: " + request.getRazaId()));
        }

        List<Color> colores = colorRepository.findByIdIn(request.getColorIds());

        Mascota mascota = Mascota.builder()
                .nombre(request.getNombreMascota())
                .especie(especie)
                .raza(raza)
                .genero(request.getGenero())
                .descripcionCaracteristicas(request.getDescripcionCaracteristicas())
                .colores(colores)
                .build();

        Reporte reporte = Reporte.builder()
                .usuarioId(usuarioId)
                .mascota(mascota)
                .tipo(request.getTipo())
                .estado(EstadoReporte.ACTIVO)
                .fechaSuceso(request.getFechaSuceso())
                .fechaReporte(LocalDateTime.now())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .direccionReferencia(request.getDireccionReferencia())
                .comuna(request.getComuna())
                .nombreContacto(request.getNombreContacto())
                .telefonoContacto(request.getTelefonoContacto())
                .emailContacto(request.getEmailContacto())
                .telefonoVisible(request.getTelefonoVisible())
                .build();

        List<String> fotosUrls = request.getFotosUrls();
        for (int i = 0; i < fotosUrls.size(); i++) {
            FotoReporte foto = FotoReporte.builder()
                    .reporte(reporte)
                    .url(fotosUrls.get(i))
                    .orden(i)
                    .esPrincipal(i == 0)
                    .build();
            reporte.getFotos().add(foto);
        }

        return toResponse(reporteRepository.save(reporte), usuarioId);
    }

    @Transactional(readOnly = true)
    public ReporteResponse obtenerReporte(Long id, Long usuarioId) {
        Reporte reporte = reporteRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
        return toResponse(reporte, usuarioId);
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listarReportes(TipoReporte tipo, EstadoReporte estado, Long usuarioId) {
        List<Reporte> reportes;
        if (tipo != null && estado != null) {
            reportes = reporteRepository.findByTipoAndEstado(tipo, estado);
        } else if (tipo != null) {
            reportes = reporteRepository.findByTipo(tipo);
        } else if (estado != null) {
            reportes = reporteRepository.findByEstado(estado);
        } else {
            reportes = reporteRepository.findAll();
        }

        return reportes.stream()
                .map(r -> toResponse(r, usuarioId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listarPorUsuario(Long usuarioId) {
        return reporteRepository.findByUsuarioId(usuarioId).stream()
                .map(r -> toResponse(r, usuarioId))
                .toList();
    }

    @Transactional
    public ReporteResponse registrarAvistamiento(Long id, Long usuarioId) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
        reporte.setEstado(EstadoReporte.RESUELTO);
        return toResponse(reporteRepository.save(reporte), usuarioId);
    }

    @Transactional
    public ReporteResponse cambiarEstado(Long id, CambiarEstadoRequest request, Long usuarioId) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));

        if (!reporte.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permiso para modificar este reporte");
        }

        reporte.setEstado(request.getEstado());
        return toResponse(reporteRepository.save(reporte), usuarioId);
    }

    // ── Mapper interno ─────────────────────────────────────────────────────

    private ReporteResponse toResponse(Reporte r, Long usuarioId) {
        Mascota m = r.getMascota();

        List<ReporteResponse.FotoDto> fotoDtos = r.getFotos().stream()
                .sorted(Comparator.comparingInt(FotoReporte::getOrden))
                .map(f -> ReporteResponse.FotoDto.builder()
                        .id(f.getId())
                        .url(f.getUrl())
                        .orden(f.getOrden())
                        .esPrincipal(f.getEsPrincipal())
                        .build())
                .toList();

        // Ocultar teléfono si no es visible y el que consulta no es el dueño
        boolean esDueno = usuarioId != null && r.getUsuarioId().equals(usuarioId);
        String telefono = (!r.getTelefonoVisible() && !esDueno) ? null : r.getTelefonoContacto();

        return ReporteResponse.builder()
                .id(r.getId())
                .usuarioId(r.getUsuarioId())
                .tipo(r.getTipo())
                .estado(r.getEstado())
                .fechaSuceso(r.getFechaSuceso())
                .fechaReporte(r.getFechaReporte())
                .nombreMascota(m != null ? m.getNombre() : null)
                .especie(m != null && m.getEspecie() != null ? m.getEspecie().getNombre() : null)
                .raza(m != null && m.getRaza() != null ? m.getRaza().getNombre() : null)
                .genero(m != null ? m.getGenero() : null)
                .descripcionCaracteristicas(m != null ? m.getDescripcionCaracteristicas() : null)
                .colores(m != null ? m.getColores().stream().map(Color::getNombre).toList() : List.of())
                .latitud(r.getLatitud())
                .longitud(r.getLongitud())
                .direccionReferencia(r.getDireccionReferencia())
                .comuna(r.getComuna())
                .nombreContacto(r.getNombreContacto())
                .telefonoContacto(telefono)
                .emailContacto(r.getEmailContacto())
                .telefonoVisible(r.getTelefonoVisible())
                .fotos(fotoDtos)
                .build();
    }
}
