package cl.duocuc.sanossalvos.geolocation.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CrearZonaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    private String nombre;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitudCentro;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitudCentro;

    @NotNull(message = "El radio es obligatorio")
    @DecimalMin(value = "0.1", message = "El radio mínimo es 0.1 km")
    @DecimalMax(value = "50.0", message = "El radio máximo es 50 km")
    private BigDecimal radioKm;
}
