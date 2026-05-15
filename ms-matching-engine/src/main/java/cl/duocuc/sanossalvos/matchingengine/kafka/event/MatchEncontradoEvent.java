package cl.duocuc.sanossalvos.matchingengine.kafka.event;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando el matching engine encuentra una coincidencia.
 * Consumido por ms-notification.
 */
public record MatchEncontradoEvent(
        Long reporteOrigenId,
        Long reporteCandidatoId,
        Long usuarioIdNotificar,
        String emailNotificar,
        String nombreMascota,
        Double distanciaKm,
        Double score,
        LocalDateTime fechaMatch
) {}
