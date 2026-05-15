package cl.duocuc.sanossalvos.notification.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReporteResueltoEvent(
        Long reporteId,
        Long usuarioId,
        String tipo,
        String emailContacto,
        String nombreMascota,
        LocalDateTime fechaResolucion
) {}
