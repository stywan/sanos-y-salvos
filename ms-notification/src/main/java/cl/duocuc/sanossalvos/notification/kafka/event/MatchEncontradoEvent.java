package cl.duocuc.sanossalvos.notification.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Evento publicado por ms-matching-engine cuando encuentra una posible coincidencia
 * entre un reporte PERDIDO y uno ENCONTRADO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
