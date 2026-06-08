package cl.duocuc.sanossalvos.petmanagement.service;

import cl.duocuc.sanossalvos.petmanagement.model.Color;
import cl.duocuc.sanossalvos.petmanagement.model.Especie;
import cl.duocuc.sanossalvos.petmanagement.model.Raza;
import cl.duocuc.sanossalvos.petmanagement.repository.ColorRepository;
import cl.duocuc.sanossalvos.petmanagement.repository.EspecieRepository;
import cl.duocuc.sanossalvos.petmanagement.repository.RazaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock EspecieRepository especieRepository;
    @Mock RazaRepository    razaRepository;
    @Mock ColorRepository   colorRepository;

    @InjectMocks CatalogoService cataloService;

    @Test
    @DisplayName("listarEspecies: delega al repositorio y devuelve lista")
    void listarEspecies_returnsFromRepo() {
        Especie especie = new Especie();
        especie.setId(1L);
        especie.setNombre("Perro");
        when(especieRepository.findAll()).thenReturn(List.of(especie));

        List<Especie> result = cataloService.listarEspecies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Perro");
    }

    @Test
    @DisplayName("listarRazasPorEspecie: filtra por especieId")
    void listarRazasPorEspecie_filtersByEspecieId() {
        Raza raza = new Raza();
        raza.setId(1L);
        raza.setNombre("Labrador");
        when(razaRepository.findByEspecieId(1L)).thenReturn(List.of(raza));

        List<Raza> result = cataloService.listarRazasPorEspecie(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Labrador");
    }

    @Test
    @DisplayName("listarColores: delega al repositorio y devuelve lista")
    void listarColores_returnsFromRepo() {
        Color color = new Color();
        color.setId(1L);
        color.setNombre("NEGRO");
        when(colorRepository.findAll()).thenReturn(List.of(color));

        List<Color> result = cataloService.listarColores();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("NEGRO");
    }
}
