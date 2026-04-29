package cl.duocuc.sanossalvos.matchingengine.dto.ext;

import lombok.Builder;
import lombok.Data;

/** Request hacia ms-notification POST /api/notificaciones */
@Data
@Builder
public class CrearNotificacionRequest {
    private Long usuarioId;
    private String tipo;       // MATCH_ENCONTRADO | ZONA_ALERTA | REPORTE_RESUELTO
    private String titulo;
    private String mensaje;
    private Long reporteId;
    private String emailDestino;
}
