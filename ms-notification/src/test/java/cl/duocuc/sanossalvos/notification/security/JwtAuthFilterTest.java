package cl.duocuc.sanossalvos.notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock JwtUtil            jwtUtil;
    @Mock HttpServletRequest  request;
    @Mock HttpServletResponse response;
    @Mock FilterChain         filterChain;
    @Mock Claims              claims;

    @InjectMocks JwtAuthFilter filter;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sin header: no autentica, continúa cadena")
    void sinHeader_noAutentica() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Token válido con rol: autentica con ROLE_USER")
    void tokenValidoConRol_autentoca() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer tok");
        when(jwtUtil.parseToken("tok")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("n@test.cl");
        when(claims.get("userId", Long.class)).thenReturn(3L);
        when(claims.get("role", String.class)).thenReturn("USER");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    @DisplayName("Token válido sin rol: autentica sin authorities")
    void tokenValidoSinRol_autenticaSinAuthority() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer tok");
        when(jwtUtil.parseToken("tok")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("n@test.cl");
        when(claims.get("userId", Long.class)).thenReturn(3L);
        when(claims.get("role", String.class)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Token inválido: no autentica, continúa cadena")
    void tokenInvalido_noAutentica() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad");
        when(jwtUtil.parseToken("bad")).thenThrow(new JwtException("invalid"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
