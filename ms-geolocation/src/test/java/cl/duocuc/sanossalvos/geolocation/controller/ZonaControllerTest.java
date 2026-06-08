package cl.duocuc.sanossalvos.geolocation.controller;

import cl.duocuc.sanossalvos.geolocation.dto.CrearZonaRequest;
import cl.duocuc.sanossalvos.geolocation.dto.ZonaResponse;
import cl.duocuc.sanossalvos.geolocation.service.ZonaBusquedaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZonaControllerTest {

    @Mock ZonaBusquedaService zonaService;
    @InjectMocks ZonaController controller;

    private Authentication auth;
    private ZonaResponse zonaResponse;

    @BeforeEach
    void setUp() {
        // credentials = userId (Long), as stored by JwtAuthFilter
        auth = new UsernamePasswordAuthenticationToken("user@test.cl", 7L, List.of());
        zonaResponse = ZonaResponse.builder()
                .id(1L).usuarioId(7L).nombre("Zona Providencia")
                .latitudCentro(BigDecimal.valueOf(-33.4372))
                .longitudCentro(BigDecimal.valueOf(-70.6506))
                .radioKm(BigDecimal.valueOf(5.0))
                .activa(true).fechaCreacion(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("crearZona: delega al servicio y retorna 201")
    void crearZona_returns201() {
        CrearZonaRequest req = new CrearZonaRequest();
        when(zonaService.crearZona(eq(req), eq(7L))).thenReturn(zonaResponse);

        ResponseEntity<ZonaResponse> resp = controller.crearZona(req, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getNombre()).isEqualTo("Zona Providencia");
        verify(zonaService).crearZona(req, 7L);
    }

    @Test
    @DisplayName("misZonas: retorna zonas del usuario autenticado")
    void misZonas_returns200() {
        when(zonaService.misZonas(7L)).thenReturn(List.of(zonaResponse));

        ResponseEntity<List<ZonaResponse>> resp = controller.misZonas(auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        verify(zonaService).misZonas(7L);
    }

    @Test
    @DisplayName("eliminarZona: retorna 204 sin body")
    void eliminarZona_returns204() {
        ResponseEntity<Void> resp = controller.eliminarZona(1L, auth);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(zonaService).eliminarZona(1L, 7L);
    }

    @Test
    @DisplayName("zonasQueContienenPunto: retorna lista de zonas cercanas sin auth")
    void zonasQueContienenPunto_returns200() {
        when(zonaService.zonasQueContienenPunto(-33.45, -70.65)).thenReturn(List.of(zonaResponse));

        ResponseEntity<List<ZonaResponse>> resp = controller.zonasQueContienenPunto(-33.45, -70.65);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
    }
}
