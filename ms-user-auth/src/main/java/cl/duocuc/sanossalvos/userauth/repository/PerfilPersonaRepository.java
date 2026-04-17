package cl.duocuc.sanossalvos.userauth.repository;

import cl.duocuc.sanossalvos.userauth.model.PerfilPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilPersonaRepository extends JpaRepository<PerfilPersona, Long> {
}
