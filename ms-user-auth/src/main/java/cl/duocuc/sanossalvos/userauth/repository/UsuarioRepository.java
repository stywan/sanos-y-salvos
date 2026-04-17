package cl.duocuc.sanossalvos.userauth.repository;

import cl.duocuc.sanossalvos.userauth.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    boolean existsByEmail(String email);

    // Carga todos los datos del usuario en una sola query (evita N+1)
    @Query("""
        SELECT u FROM Usuario u
        LEFT JOIN FETCH u.tipoUsuario
        LEFT JOIN FETCH u.roles
        LEFT JOIN FETCH u.perfilPersona
        LEFT JOIN FETCH u.perfilOrganizacion
        WHERE u.email = :email AND u.activo = true
        """)
    Optional<Usuario> findByEmailWithAll(@Param("email") String email);
}
