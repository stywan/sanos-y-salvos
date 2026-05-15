package cl.duocuc.sanossalvos.matchingengine.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReporteCreadoEvent(
        Long reporteId,
        String tipo,
        Long usuarioId,
        String especie,
        String nombreMascota,
        BigDecimal latitud,
        BigDecimal longitud,
        String comuna,
        String emailContacto,
        String nombreContacto,
        LocalDateTime fechaReporte
) {}
