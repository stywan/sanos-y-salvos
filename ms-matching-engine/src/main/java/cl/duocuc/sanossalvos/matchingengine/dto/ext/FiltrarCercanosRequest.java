package cl.duocuc.sanossalvos.matchingengine.dto.ext;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Request hacia ms-geolocation POST /api/geo/filtrar-cercanos */
@Data
@Builder
public class FiltrarCercanosRequest {

    private Double latitud;
    private Double longitud;
    private Double radioKm;
    private List<PuntoDto> puntos;

    @Data
    @Builder
    public static class PuntoDto {
        private Long id;
        private Double latitud;
        private Double longitud;
    }
}
