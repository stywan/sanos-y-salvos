package cl.duocuc.sanossalvos.petmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reportes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID del usuario autenticado que creó el reporte.
     * FK lógica hacia ms-user-auth — no hay FK física entre microservicios.
     */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Datos de la mascota — se crea junto con el reporte (CascadeType.ALL) */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoReporte tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoReporte estado = EstadoReporte.ACTIVO;

    /** Fecha en que ocurrió el extravío o avistamiento */
    @Column(name = "fecha_suceso", nullable = false)
    private LocalDate fechaSuceso;

    /** Fecha en que se publicó el reporte en el sistema */
    @Column(name = "fecha_reporte", nullable = false)
    @Builder.Default
    private LocalDateTime fechaReporte = LocalDateTime.now();

    // ── Ubicación ──────────────────────────────────────────────────────────
    @Column(precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "direccion_referencia", length = 255)
    private String direccionReferencia;

    @Column(length = 100)
    private String comuna;

    // ── Datos de contacto del reporte (paso 4 del formulario) ─────────────
    @Column(name = "nombre_contacto", nullable = false, length = 150)
    private String nombreContacto;

    @Column(name = "telefono_contacto", nullable = false, length = 20)
    private String telefonoContacto;

    @Column(name = "email_contacto", nullable = false, length = 150)
    private String emailContacto;

    /** Si false, el teléfono solo es visible para el dueño y administradores */
    @Column(name = "telefono_visible", nullable = false)
    @Builder.Default
    private Boolean telefonoVisible = true;

    // ── Galería de fotos ───────────────────────────────────────────────────
    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FotoReporte> fotos = new ArrayList<>();
}
