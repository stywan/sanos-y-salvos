package cl.duocuc.sanossalvos.petmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mascotas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la mascota (opcional — puede no conocerse en reportes de encontrada) */
    @Column(length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raza_id")
    private Raza raza;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private GeneroMascota genero;

    @Column(name = "descripcion_caracteristicas", columnDefinition = "TEXT")
    private String descripcionCaracteristicas;

    /** Colores dominantes — relación N:M con tabla intermedia mascota_colores */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mascota_colores",
            joinColumns = @JoinColumn(name = "mascota_id"),
            inverseJoinColumns = @JoinColumn(name = "color_id")
    )
    @Builder.Default
    private List<Color> colores = new ArrayList<>();
}
