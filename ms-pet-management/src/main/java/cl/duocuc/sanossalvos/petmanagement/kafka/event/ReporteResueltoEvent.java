package cl.duocuc.sanossalvos.petmanagement.kafka.event;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando un reporte cambia a estado RESUELTO (por el dueño, no por avistamiento).
 * Consumido por ms-notification.
 */
public record ReporteResueltoEvent(
        Long reporteId,
        Long usuarioId,
        String tipo,
        String emailContacto,
        String nombreMascota,
        LocalDateTime fechaResolucion
) {}
