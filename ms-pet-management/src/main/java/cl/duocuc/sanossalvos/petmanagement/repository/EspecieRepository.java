package cl.duocuc.sanossalvos.petmanagement.repository;

import cl.duocuc.sanossalvos.petmanagement.model.Especie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EspecieRepository extends JpaRepository<Especie, Long> {
    Optional<Especie> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
