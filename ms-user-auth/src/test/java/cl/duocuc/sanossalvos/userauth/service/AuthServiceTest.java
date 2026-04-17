package cl.duocuc.sanossalvos.userauth.service;

import cl.duocuc.sanossalvos.userauth.dto.LoginRequest;
import cl.duocuc.sanossalvos.userauth.dto.RegisterRequest;
import cl.duocuc.sanossalvos.userauth.dto.AuthResponse;
import cl.duocuc.sanossalvos.userauth.exception.EmailYaRegistradoException;
import cl.duocuc.sanossalvos.userauth.model.Role;
import cl.duocuc.sanossalvos.userauth.model.TipoUsuario;
import cl.duocuc.sanossalvos.userauth.model.Usuario;
import cl.duocuc.sanossalvos.userauth.repository.*;
import cl.duocuc.sanossalvos.userauth.security.JwtUtil;
import cl.duocuc.sanossalvos.userauth.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitarios")
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private TipoUsuarioRepository tipoUsuarioRepository;
    @Mock private PerfilPersonaRepository perfilPersonaRepository;
    @Mock private PerfilOrganizacionRepository perfilOrganizacionRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    private TipoUsuario tipoPersona;
    private Role roleUser;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        tipoPersona = TipoUsuario.builder().id(1L).nombre("PERSONA").build();
        roleUser    = Role.builder().id(1L).nombre("USER").build();
        usuarioMock = Usuario.builder()
                .id(1L)
                .email("juan@test.com")
                .password("encoded_pass")
                .activo(true)
                .tipoUsuario(tipoPersona)
                .roles(Set.of(roleUser))
                .build();
    }

    // ── Register ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: PERSONA exitoso retorna token")
    void register_persona_exitoso() {
        RegisterRequest request = RegisterRequest.builder()
                .email("juan@test.com").password("password123")
                .tipoUsuario("PERSONA")
                .nombre("Juan").apellido("Pérez").telefono("912345678")
                .build();

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(tipoUsuarioRepository.findByNombre("PERSONA")).thenReturn(Optional.of(tipoPersona));
        when(roleRepository.findByNombre("USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        when(usuarioRepository.save(any())).thenReturn(usuarioMock);
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(new User("juan@test.com", "encoded_pass", List.of()));
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getEmail()).isEqualTo("juan@test.com");
        assertThat(response.getTipoUsuario()).isEqualTo("PERSONA");
        assertThat(response.getNombreDisplay()).isEqualTo("Juan Pérez");
        verify(perfilPersonaRepository).save(any());
    }

    @Test
    @DisplayName("register: email duplicado lanza EmailYaRegistradoException")
    void register_emailDuplicado_lanzaExcepcion() {
        RegisterRequest request = RegisterRequest.builder()
                .email("juan@test.com").password("password123")
                .tipoUsuario("PERSONA")
                .build();

        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailYaRegistradoException.class)
                .hasMessageContaining("juan@test.com");
    }

    @Test
    @DisplayName("register: tipo inválido lanza IllegalArgumentException")
    void register_tipoInvalido_lanzaExcepcion() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@test.com").password("password123")
                .tipoUsuario("INVALIDO")
                .build();

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(tipoUsuarioRepository.findByNombre("INVALIDO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVALIDO");
    }

    @Test
    @DisplayName("register: REFUGIO crea perfil organizacion")
    void register_organizacion_creaPerfilOrganizacion() {
        TipoUsuario tipoRefugio = TipoUsuario.builder().id(3L).nombre("REFUGIO").build();
        RegisterRequest request = RegisterRequest.builder()
                .email("refugio@test.com").password("password123")
                .tipoUsuario("REFUGIO")
                .nombreOrganizacion("Refugio Esperanza")
                .descripcion("Refugio de animales").direccion("Calle 123")
                .build();

        Usuario refugioMock = Usuario.builder().id(2L).email("refugio@test.com")
                .tipoUsuario(tipoRefugio).roles(Set.of(roleUser)).build();

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(tipoUsuarioRepository.findByNombre("REFUGIO")).thenReturn(Optional.of(tipoRefugio));
        when(roleRepository.findByNombre("USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(usuarioRepository.save(any())).thenReturn(refugioMock);
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(new User("refugio@test.com", "encoded", List.of()));
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("token");

        AuthResponse response = authService.register(request);

        assertThat(response.getNombreDisplay()).isEqualTo("Refugio Esperanza");
        verify(perfilOrganizacionRepository).save(any());
        verify(perfilPersonaRepository, never()).save(any());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: credenciales correctas retorna token")
    void login_exitoso() {
        LoginRequest request = new LoginRequest("juan@test.com", "password123");

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("juan@test.com", "password123"));
        when(usuarioRepository.findByEmailWithAll("juan@test.com"))
                .thenReturn(Optional.of(usuarioMock));
        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(new User("juan@test.com", "encoded_pass", List.of()));
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    @DisplayName("login: credenciales incorrectas lanza BadCredentialsException")
    void login_credencialesInvalidas_lanzaExcepcion() {
        LoginRequest request = new LoginRequest("juan@test.com", "wrongpass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
