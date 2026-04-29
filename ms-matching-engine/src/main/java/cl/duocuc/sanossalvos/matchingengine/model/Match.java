package cl.duocuc.sanossalvos.matchingengine.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"reporte_perdido_id", "reporte_encontrado_id"}
       ))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del reporte PERDIDO en ms-pet-management */
    @Column(name = "reporte_perdido_id", nullable = false)
    private Long reportePerdidoId;

    /** ID del reporte ENCONTRADO en ms-pet-management */
    @Column(name = "reporte_encontrado_id", nullable = false)
    private Long reporteEncontradoId;

    /** Puntuación de similitud (0–100) */
    @Column(nullable = false)
    private Integer puntuacion;

    /** Distancia real entre los dos reportes en km */
    @Column(name = "distancia_km", precision = 6, scale = 3)
    private BigDecimal distanciaKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoMatch estado = EstadoMatch.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
