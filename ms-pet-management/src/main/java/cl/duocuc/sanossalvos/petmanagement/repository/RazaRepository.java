package cl.duocuc.sanossalvos.petmanagement.repository;

import cl.duocuc.sanossalvos.petmanagement.model.Raza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RazaRepository extends JpaRepository<Raza, Long> {
    List<Raza> findByEspecieId(Long especieId);
}
