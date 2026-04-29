package cl.duocuc.sanossalvos.userauth.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 200)
    private String descripcion;
}
