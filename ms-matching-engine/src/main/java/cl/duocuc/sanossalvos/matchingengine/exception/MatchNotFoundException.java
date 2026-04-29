package cl.duocuc.sanossalvos.matchingengine.exception;

public class MatchNotFoundException extends RuntimeException {
    public MatchNotFoundException(Long id) {
        super("Match no encontrado: " + id);
    }
}
