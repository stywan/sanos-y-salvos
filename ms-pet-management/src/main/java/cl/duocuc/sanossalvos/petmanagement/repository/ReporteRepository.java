package cl.duocuc.sanossalvos.petmanagement.repository;

import cl.duocuc.sanossalvos.petmanagement.model.EstadoReporte;
import cl.duocuc.sanossalvos.petmanagement.model.Reporte;
import cl.duocuc.sanossalvos.petmanagement.model.TipoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByUsuarioId(Long usuarioId);

    List<Reporte> findByTipo(TipoReporte tipo);

    List<Reporte> findByTipoAndEstado(TipoReporte tipo, EstadoReporte estado);

    List<Reporte> findByEstado(EstadoReporte estado);

    /** Carga mascota + especie en una sola query para evitar N+1 */
    @Query("SELECT r FROM Reporte r JOIN FETCH r.mascota m JOIN FETCH m.especie WHERE r.id = :id")
    Optional<Reporte> findByIdWithDetails(@Param("id") Long id);
}
