package cl.duocuc.sanossalvos.petmanagement.kafka;

import cl.duocuc.sanossalvos.petmanagement.model.*;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteEventPublisherTest {

    @Mock KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks ReporteEventPublisher publisher;

    private Reporte reporteConMascota;
    private Reporte reporteSinMascota;

    @BeforeEach
    void setUp() {
        Especie especie = Especie.builder().id(1L).nombre("Perro").build();
        Mascota mascota = Mascota.builder()
                .id(1L).nombre("Max").especie(especie).genero(GeneroMascota.MACHO).build();

        reporteConMascota = Reporte.builder()
                .id(1L).usuarioId(10L)
                .tipo(TipoReporte.PERDIDO).estado(EstadoReporte.ACTIVO)
                .mascota(mascota)
                .fechaSuceso(LocalDate.now()).fechaReporte(LocalDateTime.now())
                .emailContacto("owner@test.cl").nombreContacto("Juan")
                .telefonoContacto("+56912345678").telefonoVisible(true)
                .build();

        reporteSinMascota = Reporte.builder()
                .id(2L).usuarioId(10L)
                .tipo(TipoReporte.ENCONTRADO).estado(EstadoReporte.ACTIVO)
                .mascota(null)
                .fechaSuceso(LocalDate.now()).fechaReporte(LocalDateTime.now())
                .emailContacto("owner@test.cl").nombreContacto("Juan")
                .telefonoContacto("+56912345678").telefonoVisible(true)
                .build();

    }

    private CompletableFuture<SendResult<String, Object>> exitosoFuture() {
        SendResult<String, Object> sendResult = mock(SendResult.class);
        RecordMetadata meta = new RecordMetadata(
                new TopicPartition("topic", 0), 0L, 0, 0L, 0, 0);
        when(sendResult.getRecordMetadata()).thenReturn(meta);
        return CompletableFuture.completedFuture(sendResult);
    }

    @Test
    @DisplayName("publicarReporteCreado: reporte con mascota y especie envía evento")
    void publicarReporteCreado_conMascota_enviaEvento() {
        var future = exitosoFuture();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        publisher.publicarReporteCreado(reporteConMascota);

        verify(kafkaTemplate).send(eq(KafkaTopics.REPORTE_CREADO), anyString(), any());
    }

    @Test
    @DisplayName("publicarReporteCreado: reporte sin mascota envía evento con nulls")
    void publicarReporteCreado_sinMascota_enviaEvento() {
        var future = exitosoFuture();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        publisher.publicarReporteCreado(reporteSinMascota);

        verify(kafkaTemplate).send(eq(KafkaTopics.REPORTE_CREADO), anyString(), any());
    }

    @Test
    @DisplayName("publicarAvistamiento: envía evento con datos del avistamiento")
    void publicarAvistamiento_enviaEvento() {
        var future = exitosoFuture();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        publisher.publicarAvistamiento(reporteConMascota, 99L);

        verify(kafkaTemplate).send(eq(KafkaTopics.AVISTAMIENTO_REGISTRADO), anyString(), any());
    }

    @Test
    @DisplayName("publicarReporteResuelto: envía evento de resolución")
    void publicarReporteResuelto_enviaEvento() {
        var future = exitosoFuture();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        publisher.publicarReporteResuelto(reporteConMascota);

        verify(kafkaTemplate).send(eq(KafkaTopics.REPORTE_RESUELTO), anyString(), any());
    }

    @Test
    @DisplayName("publicarReporteResuelto: sin mascota envía evento con nombre null")
    void publicarReporteResuelto_sinMascota_enviaEvento() {
        var future = exitosoFuture();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        publisher.publicarReporteResuelto(reporteSinMascota);

        verify(kafkaTemplate).send(eq(KafkaTopics.REPORTE_RESUELTO), anyString(), any());
    }

    @Test
    @DisplayName("publish: excepción en kafkaTemplate no se propaga")
    void publish_kafkaFalla_noLanzaExcepcion() {
        CompletableFuture<SendResult<String, Object>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("Kafka down"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failed);

        publisher.publicarReporteCreado(reporteConMascota);
        // no debe lanzar
    }

    @Test
    @DisplayName("publicarReporteCreado: mascota con especie null → especie null en evento")
    void publicarReporteCreado_mascotaSinEspecie_enviaEventoConEspecieNull() {
        Mascota mascotaSinEspecie = Mascota.builder()
                .id(3L).nombre("Firulais").especie(null).genero(GeneroMascota.MACHO).build();
        Reporte reporteMascotaSinEspecie = Reporte.builder()
                .id(3L).usuarioId(10L)
                .tipo(TipoReporte.PERDIDO).estado(EstadoReporte.ACTIVO)
                .mascota(mascotaSinEspecie)
                .fechaSuceso(LocalDate.now()).fechaReporte(LocalDateTime.now())
                .emailContacto("owner@test.cl").nombreContacto("Juan")
                .telefonoContacto("+56912345678").telefonoVisible(true)
                .build();

        var future = exitosoFuture();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        publisher.publicarReporteCreado(reporteMascotaSinEspecie);

        verify(kafkaTemplate).send(eq(KafkaTopics.REPORTE_CREADO), anyString(), any());
    }

    @Test
    @DisplayName("publicarAvistamiento: mascota con especie null → especie null en evento")
    void publicarAvistamiento_mascotaSinEspecie_enviaEventoConEspecieNull() {
        Mascota mascotaSinEspecie = Mascota.builder()
                .id(4L).nombre("Michi").especie(null).genero(GeneroMascota.HEMBRA).build();
        Reporte reporteMascotaSinEspecie = Reporte.builder()
                .id(4L).usuarioId(10L)
                .tipo(TipoReporte.PERDIDO).estado(EstadoReporte.ACTIVO)
                .mascota(mascotaSinEspecie)
                .fechaSuceso(LocalDate.now()).fechaReporte(LocalDateTime.now())
                .emailContacto("owner@test.cl").nombreContacto("Juan")
                .telefonoContacto("+56912345678").telefonoVisible(true)
                .build();

        var future = exitosoFuture();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        publisher.publicarAvistamiento(reporteMascotaSinEspecie, 99L);

        verify(kafkaTemplate).send(eq(KafkaTopics.AVISTAMIENTO_REGISTRADO), anyString(), any());
    }
}
