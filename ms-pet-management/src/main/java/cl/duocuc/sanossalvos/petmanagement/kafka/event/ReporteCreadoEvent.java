package cl.duocuc.sanossalvos.petmanagement.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado cuando se crea un nuevo reporte (PERDIDO o ENCONTRADO).
 * Consumido por ms-matching-engine para disparar la búsqueda de coincidencias.
 */
public record ReporteCreadoEvent(
        Long reporteId,
        String tipo,            // "PERDIDO" | "ENCONTRADO"
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
