package cl.duocuc.sanossalvos.userauth.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfil_organizacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilOrganizacion {

    @Id
    @Column(name = "usuario_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "nombre_organizacion", nullable = false, length = 150)
    private String nombreOrganizacion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 250)
    private String direccion;

    @Column(length = 20)
    private String telefono;
}
