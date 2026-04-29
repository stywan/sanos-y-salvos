package cl.duocuc.sanossalvos.notification.dto;

import cl.duocuc.sanossalvos.notification.model.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request interno: enviado por ms-matching-engine para crear una notificación.
 */
@Data
public class CrearNotificacionRequest {

    @NotNull(message = "El usuarioId es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El tipo es obligatorio")
    private TipoNotificacion tipo;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    /** ID del reporte relacionado (opcional) */
    private Long reporteId;

    /** Email de destino para enviar correo (opcional) */
    private String emailDestino;
}
