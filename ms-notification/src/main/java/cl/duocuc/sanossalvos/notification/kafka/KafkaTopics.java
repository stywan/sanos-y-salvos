package cl.duocuc.sanossalvos.notification.kafka;

/**
 * Topics consumidos por ms-notification.
 */
public final class KafkaTopics {

    public static final String AVISTAMIENTO_REGISTRADO = "avistamiento.registrado";
    public static final String REPORTE_RESUELTO        = "reporte.resuelto";
    public static final String MATCH_ENCONTRADO        = "match.encontrado";

    private KafkaTopics() {}
}
