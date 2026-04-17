package cl.duocuc.sanossalvos.userauth.service;

import cl.duocuc.sanossalvos.userauth.dto.AuthResponse;
import cl.duocuc.sanossalvos.userauth.dto.LoginRequest;
import cl.duocuc.sanossalvos.userauth.dto.RegisterRequest;
import cl.duocuc.sanossalvos.userauth.dto.UserInfoResponse;
import cl.duocuc.sanossalvos.userauth.exception.EmailYaRegistradoException;
import cl.duocuc.sanossalvos.userauth.model.*;
import cl.duocuc.sanossalvos.userauth.repository.*;
import cl.duocuc.sanossalvos.userauth.security.JwtUtil;
import cl.duocuc.sanossalvos.userauth.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository       usuarioRepository;
    private final RoleRepository          roleRepository;
    private final TipoUsuarioRepository   tipoUsuarioRepository;
    private final PerfilPersonaRepository       perfilPersonaRepository;
    private final PerfilOrganizacionRepository  perfilOrganizacionRepository;
    private final PasswordEncoder         passwordEncoder;
    private final AuthenticationManager   authenticationManager;
    private final JwtUtil                 jwtUtil;
    private final UserDetailsServiceImpl  userDetailsService;

    // ── Factory Method: decide qué perfil crear según el tipo ──────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new EmailYaRegistradoException(
                    "El email '" + request.getEmail() + "' ya está registrado");
        }

        TipoUsuario tipo = tipoUsuarioRepository.findByNombre(request.getTipoUsuario())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de usuario inválido: " + request.getTipoUsuario()));

        Role roleUser = roleRepository.findByNombre("USER")
                .orElseThrow(() -> new IllegalStateException("Rol USER no encontrado"));

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .activo(true)
                .tipoUsuario(tipo)
                .roles(Set.of(roleUser))
                .build();

        usuario = usuarioRepository.save(usuario);

        // Factory Method: crea el perfil correcto según el tipo
        crearPerfil(usuario, request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtUtil.generateToken(userDetails, usuario.getId(), tipo.getNombre());

        return AuthResponse.builder()
                .token(token)
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .tipoUsuario(tipo.getNombre())
                .nombreDisplay(resolverNombreDesdeRequest(request))
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Usuario usuario = usuarioRepository.findByEmailWithAll(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtUtil.generateToken(
                userDetails, usuario.getId(), usuario.getTipoUsuario().getNombre());

        return AuthResponse.builder()
                .token(token)
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .tipoUsuario(usuario.getTipoUsuario().getNombre())
                .nombreDisplay(resolverNombreDesdeEntidad(usuario))
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getMe(String email) {
        Usuario usuario = usuarioRepository.findByEmailWithAll(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        UserInfoResponse.UserInfoResponseBuilder builder = UserInfoResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .tipoUsuario(usuario.getTipoUsuario().getNombre())
                .nombreDisplay(resolverNombreDesdeEntidad(usuario))
                .roles(usuario.getRoles().stream()
                        .map(r -> "ROLE_" + r.getNombre())
                        .collect(Collectors.toList()));

        if (usuario.getPerfilPersona() != null) {
            PerfilPersona p = usuario.getPerfilPersona();
            builder.nombre(p.getNombre())
                   .apellido(p.getApellido())
                   .telefono(p.getTelefono());
        } else if (usuario.getPerfilOrganizacion() != null) {
            PerfilOrganizacion o = usuario.getPerfilOrganizacion();
            builder.nombreOrganizacion(o.getNombreOrganizacion())
                   .descripcion(o.getDescripcion())
                   .direccion(o.getDireccion())
                   .telefono(o.getTelefono());
        }

        return builder.build();
    }

    // ── Factory Method privado ──────────────────────────────────────────────
    private void crearPerfil(Usuario usuario, RegisterRequest request) {
        if ("PERSONA".equalsIgnoreCase(request.getTipoUsuario())) {
            PerfilPersona perfil = PerfilPersona.builder()
                    .usuario(usuario)
                    .nombre(request.getNombre())
                    .apellido(request.getApellido())
                    .telefono(request.getTelefono())
                    .build();
            perfilPersonaRepository.save(perfil);
        } else {
            PerfilOrganizacion perfil = PerfilOrganizacion.builder()
                    .usuario(usuario)
                    .nombreOrganizacion(request.getNombreOrganizacion())
                    .descripcion(request.getDescripcion())
                    .direccion(request.getDireccion())
                    .telefono(request.getTelefono())
                    .build();
            perfilOrganizacionRepository.save(perfil);
        }
    }

    private String resolverNombreDesdeRequest(RegisterRequest request) {
        if ("PERSONA".equalsIgnoreCase(request.getTipoUsuario())) {
            return request.getNombre() + " " + request.getApellido();
        }
        return request.getNombreOrganizacion();
    }

    private String resolverNombreDesdeEntidad(Usuario usuario) {
        if (usuario.getPerfilPersona() != null) {
            return usuario.getPerfilPersona().getNombre()
                    + " " + usuario.getPerfilPersona().getApellido();
        }
        if (usuario.getPerfilOrganizacion() != null) {
            return usuario.getPerfilOrganizacion().getNombreOrganizacion();
        }
        return usuario.getEmail();
    }
}
