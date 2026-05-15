package cl.duocuc.sanossalvos.petmanagement.kafka;

/**
 * Constantes con los nombres de los topics publicados por ms-pet-management.
 * Los consumers (ms-matching-engine, ms-notification) deben usar EXACTAMENTE estos nombres.
 */
public final class KafkaTopics {

    /** Reporte recién creado (PERDIDO o ENCONTRADO). Lo consume ms-matching-engine. */
    public static final String REPORTE_CREADO = "reporte.creado";

    /** Avistamiento de una mascota perdida — el reporte original se marca RESUELTO. Lo consume ms-notification (notifica al dueño). */
    public static final String AVISTAMIENTO_REGISTRADO = "avistamiento.registrado";

    /** Reporte cuyo estado cambió a RESUELTO. Lo consume ms-notification. */
    public static final String REPORTE_RESUELTO = "reporte.resuelto";

    private KafkaTopics() {
        // utility class
    }
}
