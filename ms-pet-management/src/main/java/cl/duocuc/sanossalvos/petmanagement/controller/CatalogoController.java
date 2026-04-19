package cl.duocuc.sanossalvos.petmanagement.controller;

import cl.duocuc.sanossalvos.petmanagement.model.Color;
import cl.duocuc.sanossalvos.petmanagement.model.Especie;
import cl.duocuc.sanossalvos.petmanagement.model.Raza;
import cl.duocuc.sanossalvos.petmanagement.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService cataloService;

    @GetMapping("/especies")
    public ResponseEntity<List<Especie>> especies() {
        return ResponseEntity.ok(cataloService.listarEspecies());
    }

    @GetMapping("/razas")
    public ResponseEntity<List<Raza>> razas(@RequestParam Long especieId) {
        return ResponseEntity.ok(cataloService.listarRazasPorEspecie(especieId));
    }

    @GetMapping("/colores")
    public ResponseEntity<List<Color>> colores() {
        return ResponseEntity.ok(cataloService.listarColores());
    }
}
