package cl.duocuc.sanossalvos.matchingengine.dto;

import cl.duocuc.sanossalvos.matchingengine.model.EstadoMatch;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MatchResponse {

    private Long id;
    private Long reportePerdidoId;
    private Long reporteEncontradoId;
    private Integer puntuacion;
    private BigDecimal distanciaKm;
    private EstadoMatch estado;
    private LocalDateTime fechaCreacion;
}
