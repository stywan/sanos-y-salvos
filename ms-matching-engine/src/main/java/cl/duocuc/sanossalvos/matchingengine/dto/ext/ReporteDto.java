package cl.duocuc.sanossalvos.matchingengine.dto.ext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Representación local de un reporte de ms-pet-management.
 * Solo se mapean los campos que el matching engine necesita.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteDto {

    private Long id;
    private Long usuarioId;
    private String tipo;     // PERDIDO | ENCONTRADO
    private String estado;   // ACTIVO | RESUELTO | INACTIVO
    private String especie;
    private String genero;   // MACHO | HEMBRA | DESCONOCIDO
    private List<String> colores;
    private Double latitud;
    private Double longitud;
    private String comuna;
}
