package cl.duocuc.sanossalvos.geolocation.controller;

import cl.duocuc.sanossalvos.geolocation.dto.FiltrarCercanosRequest;
import cl.duocuc.sanossalvos.geolocation.dto.PuntoCercanoDto;
import cl.duocuc.sanossalvos.geolocation.service.ProximidadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProximidadControllerTest {

    @Mock ProximidadService proximidadService;
    @InjectMocks ProximidadController controller;

    @Test
    @DisplayName("filtrarCercanos: delega al servicio y retorna 200 con puntos")
    void filtrarCercanos_returns200() {
        PuntoCercanoDto punto = PuntoCercanoDto.builder()
                .id(1L).latitud(-33.45).longitud(-70.65).distanciaKm(2.3).build();
        when(proximidadService.filtrarCercanos(any())).thenReturn(List.of(punto));

        ResponseEntity<List<PuntoCercanoDto>> resp =
                controller.filtrarCercanos(new FiltrarCercanosRequest());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        assertThat(resp.getBody().get(0).getDistanciaKm()).isEqualTo(2.3);
    }
}
