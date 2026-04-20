package cl.duocuc.sanossalvos.geolocation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Solicitud para filtrar puntos (reportes) dentro de un radio dado.
 * Usado internamente por el BFF y el matching engine.
 */
@Data
public class FiltrarCercanosRequest {

    @NotNull
    private Double latitud;

    @NotNull
    private Double longitud;

    /** Radio en kilómetros */
    @NotNull
    private Double radioKm;

    @NotNull
    private List<PuntoDto> puntos;

    @Data
    public static class PuntoDto {
        /** ID del reporte u objeto referenciado */
        private Long id;
        private Double latitud;
        private Double longitud;
    }
}
