package cl.duocuc.sanossalvos.petmanagement.dto;

import cl.duocuc.sanossalvos.petmanagement.model.GeneroMascota;
import cl.duocuc.sanossalvos.petmanagement.model.TipoReporte;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CrearReporteRequest {

    // ── Tipo de reporte ────────────────────────────────────────────────────
    @NotNull(message = "El tipo de reporte es obligatorio (PERDIDO / ENCONTRADO)")
    private TipoReporte tipo;

    @NotNull(message = "La fecha del suceso es obligatoria")
    private LocalDate fechaSuceso;

    // ── Datos de la mascota ────────────────────────────────────────────────
    @NotNull(message = "La especie es obligatoria")
    private Long especieId;

    private Long razaId;

    private String nombreMascota;

    @NotNull(message = "El género es obligatorio")
    private GeneroMascota genero;

    private String descripcionCaracteristicas;

    private List<Long> colorIds = new ArrayList<>();

    // ── Ubicación ──────────────────────────────────────────────────────────
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String direccionReferencia;
    private String comuna;

    // ── Datos de contacto (paso 4 del formulario) ─────────────────────────
    @NotBlank(message = "El nombre de contacto es obligatorio")
    private String nombreContacto;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    private String telefonoContacto;

    @NotBlank(message = "El email de contacto es obligatorio")
    @Email(message = "El email no tiene formato válido")
    private String emailContacto;

    @NotNull(message = "Indica si el teléfono es visible públicamente")
    private Boolean telefonoVisible;

    // ── Fotos (URLs ya almacenadas en MinIO) ───────────────────────────────
    private List<String> fotosUrls = new ArrayList<>();
}
