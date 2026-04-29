package cl.duocuc.sanossalvos.matchingengine.model;

public enum EstadoMatch {
    PENDIENTE,    // Match detectado, esperando confirmación del usuario
    CONFIRMADO,   // El usuario confirmó que es su mascota
    DESCARTADO    // El usuario descartó el match
}
