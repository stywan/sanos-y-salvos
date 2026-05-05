package cl.duocuc.sanossalvos.bff.dto;

import cl.duocuc.sanossalvos.bff.dto.ext.ReporteDto;
import cl.duocuc.sanossalvos.bff.dto.ext.ZonaDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Respuesta agregada para la vista del mapa interactivo.
 * Combina reportes activos (ms-pet-management) + zonas de búsqueda (ms-geolocation).
 */
@Data
@Builder
public class MapaDto {

    /** Todos los reportes en estado ACTIVO (PERDIDO y ENCONTRADO). */
    private List<ReporteDto> reportes;

    /** Zonas de búsqueda del usuario autenticado. */
    private List<ZonaDto> zonas;
}
