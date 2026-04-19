package cl.duocuc.sanossalvos.petmanagement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;
}
