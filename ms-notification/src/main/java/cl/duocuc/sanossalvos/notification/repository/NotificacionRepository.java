package cl.duocuc.sanossalvos.notification.repository;

import cl.duocuc.sanossalvos.notification.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLeida = CURRENT_TIMESTAMP WHERE n.usuarioId = :usuarioId AND n.leida = false")
    int marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId);
}
