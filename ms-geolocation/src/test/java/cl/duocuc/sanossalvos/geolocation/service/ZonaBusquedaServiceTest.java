package cl.duocuc.sanossalvos.geolocation.service;

import cl.duocuc.sanossalvos.geolocation.dto.CrearZonaRequest;
import cl.duocuc.sanossalvos.geolocation.dto.ZonaResponse;
import cl.duocuc.sanossalvos.geolocation.exception.ZonaNotFoundException;
import cl.duocuc.sanossalvos.geolocation.model.ZonaBusqueda;
import cl.duocuc.sanossalvos.geolocation.repository.ZonaBusquedaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZonaBusquedaServiceTest {

    @Mock  ZonaBusquedaRepository zonaRepository;
    @Mock  ProximidadService proximidadService;
    @InjectMocks ZonaBusquedaService service;

    private ZonaBusqueda zonaEjemplo;

    @BeforeEach
    void setUp() {
        zonaEjemplo = ZonaBusqueda.builder()
                .id(1L)
                .usuarioId(10L)
                .nombre("Zona Parque Forestal")
                .latitudCentro(new BigDecimal("-33.4372"))
                .longitudCentro(new BigDecimal("-70.6506"))
                .radioKm(new BigDecimal("5.0"))
                .activa(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    // ── crearZona ────────────────────────────────────────────────────────────

    @Test
    void crearZona_guardaYDevuelveResponse() {
        when(zonaRepository.save(any())).thenReturn(zonaEjemplo);

        CrearZonaRequest req = new CrearZonaRequest();
        req.setNombre("Zona Parque Forestal");
        req.setLatitudCentro(new BigDecimal("-33.4372"));
        req.setLongitudCentro(new BigDecimal("-70.6506"));
        req.setRadioKm(new BigDecimal("5.0"));

        ZonaResponse resp = service.crearZona(req, 10L);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getNombre()).isEqualTo("Zona Parque Forestal");
        assertThat(resp.getActiva()).isTrue();
        verify(zonaRepository).save(any(ZonaBusqueda.class));
    }

    // ── misZonas ─────────────────────────────────────────────────────────────

    @Test
    void misZonas_devuelveSoloLasDelUsuario() {
        when(zonaRepository.findByUsuarioIdAndActivaTrue(10L)).thenReturn(List.of(zonaEjemplo));

        List<ZonaResponse> result = service.misZonas(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsuarioId()).isEqualTo(10L);
    }

    @Test
    void misZonas_usuarioSinZonas_devuelveListaVacia() {
        when(zonaRepository.findByUsuarioIdAndActivaTrue(99L)).thenReturn(List.of());
        assertThat(service.misZonas(99L)).isEmpty();
    }

    // ── eliminarZona ─────────────────────────────────────────────────────────

    @Test
    void eliminarZona_duenoDesactiva() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zonaEjemplo));
        when(zonaRepository.save(any())).thenReturn(zonaEjemplo);

        service.eliminarZona(1L, 10L);

        assertThat(zonaEjemplo.getActiva()).isFalse();
        verify(zonaRepository).save(zonaEjemplo);
    }

    @Test
    void eliminarZona_noDueno_lanzaException() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zonaEjemplo));

        assertThatThrownBy(() -> service.eliminarZona(1L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permiso");
    }

    @Test
    void eliminarZona_noExiste_lanzaNotFoundException() {
        when(zonaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarZona(999L, 10L))
                .isInstanceOf(ZonaNotFoundException.class);
    }

    // ── zonasQueContienenPunto ────────────────────────────────────────────────

    @Test
    void zonasQueContienenPunto_devuelveLasQueIncluyen() {
        when(zonaRepository.findByActivaTrue()).thenReturn(List.of(zonaEjemplo));
        // El punto está a 1.5 km del centro → dentro del radio de 5 km
        when(proximidadService.calcularDistanciaKm(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(1.5);

        List<ZonaResponse> result = service.zonasQueContienenPunto(-33.44, -70.66);

        assertThat(result).hasSize(1);
    }

    @Test
    void zonasQueContienenPunto_fueraDelRadio_devuelveVacia() {
        when(zonaRepository.findByActivaTrue()).thenReturn(List.of(zonaEjemplo));
        // El punto está a 8 km → fuera del radio de 5 km
        when(proximidadService.calcularDistanciaKm(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(8.0);

        List<ZonaResponse> result = service.zonasQueContienenPunto(-33.90, -71.20);

        assertThat(result).isEmpty();
    }
}
