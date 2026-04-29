package cl.duocuc.sanossalvos.matchingengine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Solicitud para iniciar la búsqueda de matches de un reporte.
 * Llamado por el BFF o el frontend justo después de crear un reporte.
 */
@Data
public class BuscarMatchesRequest {

    @NotNull(message = "El reporteId es obligatorio")
    private Long reporteId;
}
