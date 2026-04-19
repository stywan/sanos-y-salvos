package cl.duocuc.sanossalvos.petmanagement.service;

import cl.duocuc.sanossalvos.petmanagement.model.Color;
import cl.duocuc.sanossalvos.petmanagement.model.Especie;
import cl.duocuc.sanossalvos.petmanagement.model.Raza;
import cl.duocuc.sanossalvos.petmanagement.repository.ColorRepository;
import cl.duocuc.sanossalvos.petmanagement.repository.EspecieRepository;
import cl.duocuc.sanossalvos.petmanagement.repository.RazaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final EspecieRepository especieRepository;
    private final RazaRepository razaRepository;
    private final ColorRepository colorRepository;

    @Transactional(readOnly = true)
    public List<Especie> listarEspecies() {
        return especieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Raza> listarRazasPorEspecie(Long especieId) {
        return razaRepository.findByEspecieId(especieId);
    }

    @Transactional(readOnly = true)
    public List<Color> listarColores() {
        return colorRepository.findAll();
    }
}
