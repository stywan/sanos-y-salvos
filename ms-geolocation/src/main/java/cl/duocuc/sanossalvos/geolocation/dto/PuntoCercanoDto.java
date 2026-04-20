package cl.duocuc.sanossalvos.geolocation.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Resultado de un punto que quedó dentro del radio de búsqueda.
 */
@Data
@Builder
public class PuntoCercanoDto {

    private Long id;
    private Double latitud;
    private Double longitud;
    /** Distancia real al centro, en kilómetros (redondeado a 2 decimales) */
    private Double distanciaKm;
}
