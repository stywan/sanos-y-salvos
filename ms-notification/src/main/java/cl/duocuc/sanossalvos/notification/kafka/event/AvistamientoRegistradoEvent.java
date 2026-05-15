package cl.duocuc.sanossalvos.notification.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AvistamientoRegistradoEvent(
        Long reporteId,
        Long usuarioIdDueno,
        Long usuarioIdReportador,
        String emailDueno,
        String nombreMascota,
        String especie,
        String comuna,
        LocalDateTime fechaAvistamiento
) {}
