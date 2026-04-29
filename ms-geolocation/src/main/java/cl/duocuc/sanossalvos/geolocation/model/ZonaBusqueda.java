package cl.duocuc.sanossalvos.geolocation.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "zonas_busqueda")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ZonaBusqueda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK lógica hacia ms-user-auth */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(name = "latitud_centro", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitudCentro;

    @Column(name = "longitud_centro", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitudCentro;

    /** Radio en kilómetros dentro del cual el usuario quiere recibir alertas */
    @Column(name = "radio_km", precision = 6, scale = 3, nullable = false)
    private BigDecimal radioKm;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
