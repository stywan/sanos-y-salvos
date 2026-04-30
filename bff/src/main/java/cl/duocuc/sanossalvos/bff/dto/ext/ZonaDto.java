package cl.duocuc.sanossalvos.bff.dto.ext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO externo: refleja ZonaResponse de ms-geolocation.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZonaDto {

    private Long id;
    private Long usuarioId;
    private String nombre;
    private BigDecimal latitudCentro;
    private BigDecimal longitudCentro;
    private BigDecimal radioKm;
    private Boolean activa;
    private LocalDateTime fechaCreacion;
}
