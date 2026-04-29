package cl.duocuc.sanossalvos.petmanagement.repository;

import cl.duocuc.sanossalvos.petmanagement.model.FotoReporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FotoReporteRepository extends JpaRepository<FotoReporte, Long> {
    List<FotoReporte> findByReporteIdOrderByOrden(Long reporteId);
}
