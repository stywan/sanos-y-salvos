package cl.duocuc.sanossalvos.notification.exception;

public class NotificacionNotFoundException extends RuntimeException {

    public NotificacionNotFoundException(Long id) {
        super("Notificación no encontrada: " + id);
    }
}
