package cl.duocuc.sanossalvos.bff.dto.ext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO externo: refleja ReporteResponse de ms-pet-management.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteDto {

    private Long id;
    private Long usuarioId;
    private String tipo;           // PERDIDO | ENCONTRADO
    private String estado;         // ACTIVO | RESUELTO | ARCHIVADO

    private LocalDate fechaSuceso;
    private LocalDateTime fechaReporte;

    // Mascota
    private String nombreMascota;
    private String especie;
    private String raza;
    private String genero;
    private String descripcionCaracteristicas;
    private List<String> colores;

    // Ubicación
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String direccionReferencia;
    private String comuna;

    // Contacto
    private String nombreContacto;
    private String telefonoContacto;
    private String emailContacto;
    private Boolean telefonoVisible;

    // Fotos
    private List<FotoDto> fotos;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FotoDto {
        private Long id;
        private String url;
        private Integer orden;
        private Boolean esPrincipal;
    }
}
