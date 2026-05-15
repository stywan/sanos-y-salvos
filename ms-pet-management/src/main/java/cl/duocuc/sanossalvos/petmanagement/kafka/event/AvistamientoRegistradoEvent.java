package cl.duocuc.sanossalvos.petmanagement.kafka.event;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando alguien reporta que vio/encontró una mascota perdida.
 * El reporte original (PERDIDO) se marca como RESUELTO automáticamente.
 * Consumido por ms-notification para avisar al dueño.
 */
public record AvistamientoRegistradoEvent(
        Long reporteId,
        Long usuarioIdDueno,            // a quién notificar (dueño del reporte original)
        Long usuarioIdReportador,       // quién registró el avistamiento
        String emailDueno,
        String nombreMascota,
        String especie,
        String comuna,
        LocalDateTime fechaAvistamiento
) {}
