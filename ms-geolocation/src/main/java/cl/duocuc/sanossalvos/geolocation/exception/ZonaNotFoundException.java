package cl.duocuc.sanossalvos.geolocation.exception;

public class ZonaNotFoundException extends RuntimeException {

    public ZonaNotFoundException(Long id) {
        super("Zona de búsqueda no encontrada: " + id);
    }
}
