package cl.duocuc.sanossalvos.matchingengine.repository;

import cl.duocuc.sanossalvos.matchingengine.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByReportePerdidoIdOrderByPuntuacionDesc(Long reportePerdidoId);

    List<Match> findByReporteEncontradoIdOrderByPuntuacionDesc(Long reporteEncontradoId);

    Optional<Match> findByReportePerdidoIdAndReporteEncontradoId(
            Long reportePerdidoId, Long reporteEncontradoId);
}
