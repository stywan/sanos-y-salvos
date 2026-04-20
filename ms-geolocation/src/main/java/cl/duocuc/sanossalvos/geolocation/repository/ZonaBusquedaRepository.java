package cl.duocuc.sanossalvos.geolocation.repository;

import cl.duocuc.sanossalvos.geolocation.model.ZonaBusqueda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZonaBusquedaRepository extends JpaRepository<ZonaBusqueda, Long> {

    List<ZonaBusqueda> findByUsuarioIdAndActivaTrue(Long usuarioId);

    List<ZonaBusqueda> findByActivaTrue();
}
