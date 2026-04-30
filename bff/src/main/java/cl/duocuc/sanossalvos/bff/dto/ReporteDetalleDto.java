package cl.duocuc.sanossalvos.bff.dto;

import cl.duocuc.sanossalvos.bff.dto.ext.MatchDto;
import cl.duocuc.sanossalvos.bff.dto.ext.ReporteDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Respuesta agregada para el detalle de un reporte.
 * Combina el reporte (ms-pet-management) con sus matches (ms-matching-engine).
 */
@Data
@Builder
public class ReporteDetalleDto {

    /** Información completa del reporte. */
    private ReporteDto reporte;

    /** Matches asociados a este reporte, ordenados por puntuación descendente. */
    private List<MatchDto> matches;
}
