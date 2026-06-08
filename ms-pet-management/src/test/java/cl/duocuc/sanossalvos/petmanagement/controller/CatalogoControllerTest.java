package cl.duocuc.sanossalvos.petmanagement.controller;

import cl.duocuc.sanossalvos.petmanagement.model.Color;
import cl.duocuc.sanossalvos.petmanagement.model.Especie;
import cl.duocuc.sanossalvos.petmanagement.model.Raza;
import cl.duocuc.sanossalvos.petmanagement.service.CatalogoService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoControllerTest {

    @Mock CatalogoService cataloService;
    @InjectMocks CatalogoController controller;

    @Test
    @DisplayName("especies: retorna 200 con lista de especies")
    void especies_returns200() {
        Especie especie = Especie.builder().id(1L).nombre("Perro").build();
        when(cataloService.listarEspecies()).thenReturn(List.of(especie));

        ResponseEntity<List<Especie>> resp = controller.especies();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        assertThat(resp.getBody().get(0).getNombre()).isEqualTo("Perro");
    }

    @Test
    @DisplayName("razas: retorna 200 con razas filtradas por especieId")
    void razas_returns200() {
        Especie especie = Especie.builder().id(1L).nombre("Perro").build();
        Raza raza = Raza.builder().id(1L).nombre("Labrador").especie(especie).build();
        when(cataloService.listarRazasPorEspecie(1L)).thenReturn(List.of(raza));

        ResponseEntity<List<Raza>> resp = controller.razas(1L);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        assertThat(resp.getBody().get(0).getNombre()).isEqualTo("Labrador");
    }

    @Test
    @DisplayName("colores: retorna 200 con lista de colores")
    void colores_returns200() {
        Color color = Color.builder().id(1L).nombre("NEGRO").build();
        when(cataloService.listarColores()).thenReturn(List.of(color));

        ResponseEntity<List<Color>> resp = controller.colores();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(1);
        assertThat(resp.getBody().get(0).getNombre()).isEqualTo("NEGRO");
    }
}
