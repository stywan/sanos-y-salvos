package cl.duocuc.sanossalvos.bff.dto;

import cl.duocuc.sanossalvos.bff.dto.ext.MatchDto;
import cl.duocuc.sanossalvos.bff.dto.ext.ReporteDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Respuesta agregada para el dashboard del usuario.
 * Combina estadísticas de ms-pet-management, ms-matching-engine y ms-notification.
 */
@Data
@Builder
public class DashboardDto {

    /** Cantidad de reportes de tipo PERDIDO del usuario. */
    private int totalReportesPerdidos;

    /** Cantidad de reportes de tipo ENCONTRADO del usuario. */
    private int totalReportesEncontrados;

    /** Cantidad de matches en estado PENDIENTE asociados al reporte más reciente. */
    private int totalMatchesPendientes;

    /** Cantidad de notificaciones no leídas del usuario. */
    private long notificacionesNoLeidas;

    /** Últimos 3 reportes del usuario (más recientes primero). */
    private List<ReporteDto> reportesRecientes;

    /** Matches del reporte más reciente del usuario. */
    private List<MatchDto> matchesRecientes;
}
