package cl.duocuc.sanossalvos.petmanagement.config;

import cl.duocuc.sanossalvos.petmanagement.model.Color;
import cl.duocuc.sanossalvos.petmanagement.model.Especie;
import cl.duocuc.sanossalvos.petmanagement.model.Raza;
import cl.duocuc.sanossalvos.petmanagement.repository.ColorRepository;
import cl.duocuc.sanossalvos.petmanagement.repository.EspecieRepository;
import cl.duocuc.sanossalvos.petmanagement.repository.RazaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final EspecieRepository especieRepo;
    private final RazaRepository razaRepo;
    private final ColorRepository colorRepo;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedEspeciesYRazas();
        seedColores();
    }

    private void seedEspeciesYRazas() {
        Map<String, List<String>> especiesRazas = Map.of(
            "Perro", List.of("Golden Retriever", "Labrador", "Beagle", "Bulldog",
                             "Pastor Alemán", "Poodle", "Chihuahua", "Mestizo"),
            "Gato", List.of("Siamés", "Persa", "Maine Coon", "Bengalí",
                            "Ragdoll", "Mestizo"),
            "Ave",     List.of("Loro", "Canario", "Periquito", "Cacatúa"),
            "Conejo",  List.of("Mini Lop", "Angora", "Rex", "Mestizo"),
            "Otro",    List.of("No especificado")
        );

        especiesRazas.forEach((nombreEspecie, razas) -> {
            Especie especie = especieRepo.findByNombreIgnoreCase(nombreEspecie)
                    .orElseGet(() -> especieRepo.save(
                            Especie.builder().nombre(nombreEspecie).build()));

            razas.forEach(nombreRaza -> {
                boolean existe = razaRepo.findByEspecieId(especie.getId())
                        .stream().anyMatch(r -> r.getNombre().equalsIgnoreCase(nombreRaza));
                if (!existe) {
                    razaRepo.save(Raza.builder().nombre(nombreRaza).especie(especie).build());
                }
            });
        });

        log.info("Especies y razas inicializadas");
    }

    private void seedColores() {
        List<String> colores = List.of(
            "Negro", "Blanco", "Café", "Marrón", "Gris", "Amarillo",
            "Naranja", "Atigrado", "Manchado", "Tricolor", "Beige", "Dorado"
        );

        colores.forEach(nombre -> {
            if (colorRepo.findAll().stream().noneMatch(c -> c.getNombre().equalsIgnoreCase(nombre))) {
                colorRepo.save(Color.builder().nombre(nombre).build());
            }
        });

        log.info("Colores inicializados");
    }
}
