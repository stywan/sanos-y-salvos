package cl.duocuc.sanossalvos.bff.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private static final String SECRET = "clave_local_desarrollo_no_usar_en_produccion_para_tests";

    private JwtAuthFilter filter;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        filter = new JwtAuthFilter(new JwtUtil(SECRET));
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String buildToken(String email, Long userId, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("doFilterInternal: sin cabecera Authorization no establece contexto de seguridad")
    void doFilterInternal_noHeader_noAuthentication() throws Exception {
        HttpServletRequest  req   = mock(HttpServletRequest.class);
        HttpServletResponse resp  = mock(HttpServletResponse.class);
        FilterChain         chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(req, resp, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, resp);
    }

    @Test
    @DisplayName("doFilterInternal: con JWT válido establece autenticación en SecurityContext")
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        String token  = buildToken("user@test.cl", 7L, "USER");
        String header = "Bearer " + token;

        HttpServletRequest  req   = mock(HttpServletRequest.class);
        HttpServletResponse resp  = mock(HttpServletResponse.class);
        FilterChain         chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn(header);

        filter.doFilterInternal(req, resp, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("user@test.cl");
        assertThat(auth.getDetails()).isEqualTo(header);
        verify(chain).doFilter(req, resp);
    }

    @Test
    @DisplayName("doFilterInternal: con JWT inválido no establece autenticación pero continúa la cadena")
    void doFilterInternal_invalidToken_noAuthenticationButChainContinues() throws Exception {
        HttpServletRequest  req   = mock(HttpServletRequest.class);
        HttpServletResponse resp  = mock(HttpServletResponse.class);
        FilterChain         chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer token.invalido.aqui");

        filter.doFilterInternal(req, resp, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, resp);
    }

    @Test
    @DisplayName("doFilterInternal: JWT con role null no asigna authorities")
    void doFilterInternal_tokenWithNullRole_noAuthorities() throws Exception {
        String token = Jwts.builder()
                .subject("user@test.cl")
                .claim("userId", 1L)
                // sin claim "role"
                .signWith(key)
                .compact();

        HttpServletRequest  req   = mock(HttpServletRequest.class);
        HttpServletResponse resp  = mock(HttpServletResponse.class);
        FilterChain         chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(req, resp, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
    }
}
