package cl.duocuc.sanossalvos.petmanagement.dto;

import cl.duocuc.sanossalvos.petmanagement.model.EstadoReporte;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarEstadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoReporte estado;
}
