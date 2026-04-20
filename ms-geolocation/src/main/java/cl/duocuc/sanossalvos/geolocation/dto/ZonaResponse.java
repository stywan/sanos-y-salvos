package cl.duocuc.sanossalvos.geolocation.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ZonaResponse {

    private Long id;
    private Long usuarioId;
    private String nombre;
    private BigDecimal latitudCentro;
    private BigDecimal longitudCentro;
    private BigDecimal radioKm;
    private Boolean activa;
    private LocalDateTime fechaCreacion;
}
