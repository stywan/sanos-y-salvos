package cl.duocuc.sanossalvos.userauth.repository;

import cl.duocuc.sanossalvos.userauth.model.PerfilOrganizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilOrganizacionRepository extends JpaRepository<PerfilOrganizacion, Long> {
}
