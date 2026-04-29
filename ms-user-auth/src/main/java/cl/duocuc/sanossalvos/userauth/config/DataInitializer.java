package cl.duocuc.sanossalvos.userauth.config;

import cl.duocuc.sanossalvos.userauth.model.Role;
import cl.duocuc.sanossalvos.userauth.model.TipoUsuario;
import cl.duocuc.sanossalvos.userauth.repository.RoleRepository;
import cl.duocuc.sanossalvos.userauth.repository.TipoUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;

    @Override
    public void run(String... args) {
        seedRoles();
        seedTiposUsuario();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                Role.builder().nombre("USER").descripcion("Usuario estándar del sistema").build(),
                Role.builder().nombre("ADMIN").descripcion("Administrador del sistema").build()
            ));
            log.info("Roles iniciales creados: USER, ADMIN");
        }
    }

    private void seedTiposUsuario() {
        if (tipoUsuarioRepository.count() == 0) {
            tipoUsuarioRepository.saveAll(List.of(
                TipoUsuario.builder()
                    .nombre("PERSONA")
                    .descripcion("Dueño o ciudadano particular")
                    .build(),
                TipoUsuario.builder()
                    .nombre("VETERINARIA")
                    .descripcion("Clínica veterinaria colaboradora")
                    .build(),
                TipoUsuario.builder()
                    .nombre("REFUGIO")
                    .descripcion("Refugio de animales")
                    .build(),
                TipoUsuario.builder()
                    .nombre("MUNICIPALIDAD")
                    .descripcion("Municipalidad colaboradora")
                    .build()
            ));
            log.info("Tipos de usuario creados: PERSONA, VETERINARIA, REFUGIO, MUNICIPALIDAD");
        }
    }
}
