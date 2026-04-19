package cl.duocuc.sanossalvos.petmanagement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fotos_reporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FotoReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id", nullable = false)
    private Reporte reporte;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(name = "es_principal", nullable = false)
    @Builder.Default
    private Boolean esPrincipal = false;
}
