package cl.duocuc.sanossalvos.geolocation.service;

import cl.duocuc.sanossalvos.geolocation.dto.FiltrarCercanosRequest;
import cl.duocuc.sanossalvos.geolocation.dto.PuntoCercanoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProximidadServiceTest {

    private ProximidadService service;

    @BeforeEach
    void setUp() {
        service = new ProximidadService();
    }

    // ── Haversine ────────────────────────────────────────────────────────────

    @Test
    void distanciaEntreMismoPunto_esZero() {
        double dist = service.calcularDistanciaKm(-33.45, -70.65, -33.45, -70.65);
        assertThat(dist).isLessThan(0.001);
    }

    @Test
    void distanciaSantiagoValparaiso_cercaA100km() {
        // Santiago ↔ Valparaíso ≈ 100 km en línea recta (Haversine)
        double dist = service.calcularDistanciaKm(-33.4569, -70.6483, -33.0472, -71.6127);
        assertThat(dist).isBetween(95.0, 110.0);
    }

    // ── filtrarCercanos ──────────────────────────────────────────────────────

    @Test
    void filtraCorrectamente_soloLosDelRadio() {
        FiltrarCercanosRequest req = buildRequest(-33.45, -70.65, 5.0,
                List.of(
                        punto(1L, -33.46, -70.66),   // ~1.4 km  → dentro
                        punto(2L, -33.90, -71.20),   // ~70 km   → fuera
                        punto(3L, -33.47, -70.67)    // ~3 km    → dentro
                ));

        List<PuntoCercanoDto> resultado = service.filtrarCercanos(req);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(PuntoCercanoDto::getId).containsExactly(1L, 3L);
    }

    @Test
    void resultadoOrdenadoDeMasCercanoAMasLejano() {
        FiltrarCercanosRequest req = buildRequest(-33.45, -70.65, 10.0,
                List.of(
                        punto(10L, -33.47, -70.67),  // ~3 km
                        punto(20L, -33.46, -70.655), // ~1 km aprox
                        punto(30L, -33.49, -70.69)   // ~5 km aprox
                ));

        List<PuntoCercanoDto> resultado = service.filtrarCercanos(req);

        assertThat(resultado).hasSize(3);
        double primero = resultado.get(0).getDistanciaKm();
        double segundo = resultado.get(1).getDistanciaKm();
        double tercero = resultado.get(2).getDistanciaKm();
        assertThat(primero).isLessThanOrEqualTo(segundo);
        assertThat(segundo).isLessThanOrEqualTo(tercero);
    }

    @Test
    void sinPuntosDentroDelRadio_devuelveListaVacia() {
        FiltrarCercanosRequest req = buildRequest(-33.45, -70.65, 1.0,
                List.of(punto(1L, -34.90, -71.20)));

        List<PuntoCercanoDto> resultado = service.filtrarCercanos(req);

        assertThat(resultado).isEmpty();
    }

    @Test
    void listaVaciaDeEntrada_devuelveListaVacia() {
        FiltrarCercanosRequest req = buildRequest(-33.45, -70.65, 10.0, List.of());
        assertThat(service.filtrarCercanos(req)).isEmpty();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private FiltrarCercanosRequest buildRequest(double lat, double lon, double radio,
                                                List<FiltrarCercanosRequest.PuntoDto> puntos) {
        FiltrarCercanosRequest req = new FiltrarCercanosRequest();
        req.setLatitud(lat);
        req.setLongitud(lon);
        req.setRadioKm(radio);
        req.setPuntos(puntos);
        return req;
    }

    private FiltrarCercanosRequest.PuntoDto punto(Long id, double lat, double lon) {
        FiltrarCercanosRequest.PuntoDto p = new FiltrarCercanosRequest.PuntoDto();
        p.setId(id);
        p.setLatitud(lat);
        p.setLongitud(lon);
        return p;
    }
}
