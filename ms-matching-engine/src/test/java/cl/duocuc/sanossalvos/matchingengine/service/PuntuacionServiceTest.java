package cl.duocuc.sanossalvos.matchingengine.service;

import cl.duocuc.sanossalvos.matchingengine.dto.ext.ReporteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PuntuacionServiceTest {

    private PuntuacionService service;

    @BeforeEach
    void setUp() {
        service = new PuntuacionService();
    }

    // ── Especie ───────────────────────────────────────────────────────────────

    @Test
    void especieDistinta_retornaCero() {
        ReporteDto perdido    = reporte("Perro", "MACHO",   List.of("Negro"));
        ReporteDto encontrado = reporte("Gato",  "MACHO",   List.of("Negro"));
        assertThat(service.calcular(perdido, encontrado, 1.0)).isEqualTo(0);
    }

    @Test
    void especieNula_retornaCero() {
        ReporteDto perdido    = reporte(null,    "MACHO", List.of("Negro"));
        ReporteDto encontrado = reporte("Perro", "MACHO", List.of("Negro"));
        assertThat(service.calcular(perdido, encontrado, 1.0)).isEqualTo(0);
    }

    // ── Puntuación máxima ─────────────────────────────────────────────────────

    @Test
    void matchPerfecto_retorna100() {
        // misma especie + mismos colores (40) + distancia <2km (40) + mismo genero (20) = 100
        ReporteDto perdido    = reporte("Perro", "MACHO", List.of("Negro", "Blanco"));
        ReporteDto encontrado = reporte("Perro", "MACHO", List.of("Negro", "Blanco"));
        assertThat(service.calcular(perdido, encontrado, 0.5)).isEqualTo(100);
    }

    // ── Colores ───────────────────────────────────────────────────────────────

    @Test
    void coloresVacios_retornaCeroPuntosColores() {
        assertThat(service.puntosColores(List.of(), List.of("Negro"))).isEqualTo(0);
        assertThat(service.puntosColores(null, List.of("Negro"))).isEqualTo(0);
    }

    @Test
    void coloresTotalmenteComunes_retorna40() {
        assertThat(service.puntosColores(List.of("Negro", "Blanco"), List.of("Negro", "Blanco")))
                .isEqualTo(40);
    }

    @Test
    void coloresSinInterseccion_retornaCero() {
        assertThat(service.puntosColores(List.of("Negro"), List.of("Blanco"))).isEqualTo(0);
    }

    @Test
    void coloresParciales_retornaProporcional() {
        // 1 de 2 en común → 50% de 40 = 20
        assertThat(service.puntosColores(List.of("Negro", "Blanco"), List.of("Negro", "Café")))
                .isEqualTo(20);
    }

    // ── Distancia ─────────────────────────────────────────────────────────────

    @Test
    void distanciaMenor2km_retorna40() {
        assertThat(service.puntosDistancia(1.5)).isEqualTo(40);
    }

    @Test
    void distanciaEntre2y5_retorna30() {
        assertThat(service.puntosDistancia(3.0)).isEqualTo(30);
    }

    @Test
    void distanciaEntre5y10_retorna20() {
        assertThat(service.puntosDistancia(7.0)).isEqualTo(20);
    }

    @Test
    void distanciaEntre10y20_retorna10() {
        assertThat(service.puntosDistancia(15.0)).isEqualTo(10);
    }

    @Test
    void distanciaMayorIgual20_retornaCero() {
        assertThat(service.puntosDistancia(25.0)).isEqualTo(0);
    }

    // ── Género ────────────────────────────────────────────────────────────────

    @Test
    void generoIgual_retorna20() {
        assertThat(service.puntosGenero("MACHO", "MACHO")).isEqualTo(20);
    }

    @Test
    void generoDistinto_retornaCero() {
        assertThat(service.puntosGenero("MACHO", "HEMBRA")).isEqualTo(0);
    }

    @Test
    void generoDesconocido_retornaCero() {
        assertThat(service.puntosGenero("MACHO", "DESCONOCIDO")).isEqualTo(0);
        assertThat(service.puntosGenero("DESCONOCIDO", "DESCONOCIDO")).isEqualTo(0);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private ReporteDto reporte(String especie, String genero, List<String> colores) {
        ReporteDto r = new ReporteDto();
        r.setEspecie(especie);
        r.setGenero(genero);
        r.setColores(colores);
        return r;
    }
}
