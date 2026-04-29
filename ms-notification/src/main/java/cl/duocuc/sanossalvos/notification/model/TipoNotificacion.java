package cl.duocuc.sanossalvos.notification.model;

public enum TipoNotificacion {
    MATCH_ENCONTRADO,   // Posible match entre un perdido y un encontrado
    ZONA_ALERTA,        // Se encontró una mascota dentro de una zona de búsqueda del usuario
    REPORTE_RESUELTO    // El dueño marcó su reporte como RESUELTO
}
