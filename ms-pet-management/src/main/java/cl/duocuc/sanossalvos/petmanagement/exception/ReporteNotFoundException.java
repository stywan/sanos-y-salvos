package cl.duocuc.sanossalvos.petmanagement.exception;

public class ReporteNotFoundException extends RuntimeException {
    public ReporteNotFoundException(Long id) {
        super("Reporte no encontrado con id: " + id);
    }
}
