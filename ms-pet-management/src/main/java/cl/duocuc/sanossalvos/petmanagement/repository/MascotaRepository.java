package cl.duocuc.sanossalvos.petmanagement.repository;

import cl.duocuc.sanossalvos.petmanagement.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
}
