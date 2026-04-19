package cl.duocuc.sanossalvos.petmanagement.dto;

import cl.duocuc.sanossalvos.petmanagement.model.EstadoReporte;
import cl.duocuc.sanossalvos.petmanagement.model.GeneroMascota;
import cl.duocuc.sanossalvos.petmanagement.model.TipoReporte;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReporteResponse {

    private Long id;
    private Long usuarioId;
    private TipoReporte tipo;
    private EstadoReporte estado;
    private LocalDate fechaSuceso;
    private LocalDateTime fechaReporte;

    // Mascota
    private String nombreMascota;
    private String especie;
    private String raza;
    private GeneroMascota genero;
    private String descripcionCaracteristicas;
    private List<String> colores;

    // Ubicación
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String direccionReferencia;
    private String comuna;

    // Contacto (telefonoContacto puede ser null si telefonoVisible=false y no es el dueño)
    private String nombreContacto;
    private String telefonoContacto;
    private String emailContacto;
    private Boolean telefonoVisible;

    // Fotos
    private List<FotoDto> fotos;

    @Data
    @Builder
    public static class FotoDto {
        private Long id;
        private String url;
        private Integer orden;
        private Boolean esPrincipal;
    }
}
