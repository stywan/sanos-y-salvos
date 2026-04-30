package cl.duocuc.sanossalvos.bff.dto.ext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO externo: refleja MatchResponse de ms-matching-engine.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchDto {

    private Long id;
    private Long reportePerdidoId;
    private Long reporteEncontradoId;
    private Integer puntuacion;
    private BigDecimal distanciaKm;
    private String estado;          // PENDIENTE | CONFIRMADO | DESCARTADO
    private LocalDateTime fechaCreacion;
}
