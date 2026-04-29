package cl.duocuc.sanossalvos.matchingengine.dto.ext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Respuesta de ms-geolocation POST /api/geo/filtrar-cercanos */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PuntoCercanoDto {
    private Long id;
    private Double latitud;
    private Double longitud;
    private Double distanciaKm;
}
