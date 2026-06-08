package cl.duocuc.sanossalvos.userauth.security;

import cl.duocuc.sanossalvos.userauth.model.Role;
import cl.duocuc.sanossalvos.userauth.model.Usuario;
import cl.duocuc.sanossalvos.userauth.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks UserDetailsServiceImpl service;

    @Test
    @DisplayName("loadUserByUsername: usuario activo retorna UserDetails con authorities")
    void loadUserByUsername_encontrado_retornaUserDetails() {
        Role roleUser = Role.builder().id(1L).nombre("USER").build();
        Usuario usuario = Usuario.builder()
                .id(1L).email("a@b.cl").password("encoded").activo(true)
                .roles(Set.of(roleUser)).build();

        when(usuarioRepository.findByEmailAndActivoTrue("a@b.cl")).thenReturn(Optional.of(usuario));

        UserDetails details = service.loadUserByUsername("a@b.cl");

        assertThat(details.getUsername()).isEqualTo("a@b.cl");
        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    @DisplayName("loadUserByUsername: no encontrado lanza UsernameNotFoundException")
    void loadUserByUsername_noEncontrado_lanzaExcepcion() {
        when(usuarioRepository.findByEmailAndActivoTrue("x@x.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("x@x.cl"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("x@x.cl");
    }
}
