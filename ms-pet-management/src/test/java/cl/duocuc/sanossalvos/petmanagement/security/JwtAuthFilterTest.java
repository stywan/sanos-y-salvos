package cl.duocuc.sanossalvos.petmanagement.security;

import io.jsonwebtoken.Claims;
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

import java.util.List;

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
    @DisplayName("Token válido, isValid=true: autentica con email como principal")
    void tokenValido_autenticaUsuario() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer validtok");
        when(jwtUtil.isValid("validtok")).thenReturn(true);
        when(jwtUtil.getClaims("validtok")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("pet@test.cl");
        when(claims.get("userId", Long.class)).thenReturn(3L);
        when(claims.get("roles")).thenReturn(List.of("USER"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo("pet@test.cl");
    }

    @Test
    @DisplayName("Token válido sin roles: autentica con authorities vacías")
    void tokenValidoSinRoles_autenticaSinAuthorities() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer validtok");
        when(jwtUtil.isValid("validtok")).thenReturn(true);
        when(jwtUtil.getClaims("validtok")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("pet@test.cl");
        when(claims.get("userId", Long.class)).thenReturn(3L);
        when(claims.get("roles")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Token inválido (isValid=false): no autentica, continúa cadena")
    void tokenInvalido_noAutentica() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer badtok");
        when(jwtUtil.isValid("badtok")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Header sin 'Bearer ': no autentica, continúa cadena")
    void headerSinBearer_noAutentica() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
