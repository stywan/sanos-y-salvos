package cl.duocuc.sanossalvos.petmanagement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "razas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "especie_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Raza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;
}
