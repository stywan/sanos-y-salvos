package cl.duocuc.sanossalvos.bff.dto.ext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO externo: refleja NotificacionResponse de ms-notification.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificacionDto {

    private Long id;
    private Long usuarioId;
    private String tipo;       // MATCH_ENCONTRADO | ZONA_ALERTA | REPORTE_RESUELTO
    private String titulo;
    private String mensaje;
    private Long reporteId;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLeida;
}
