package cl.duocuc.sanossalvos.notification.dto;

import cl.duocuc.sanossalvos.notification.model.TipoNotificacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificacionResponse {

    private Long id;
    private Long usuarioId;
    private TipoNotificacion tipo;
    private String titulo;
    private String mensaje;
    private Long reporteId;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLeida;
}
