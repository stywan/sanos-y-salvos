package cl.duocuc.sanossalvos.userauth.security;

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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock JwtUtil jwtUtil;
    @Mock UserDetailsService userDetailsService;
    @Mock HttpServletRequest  request;
    @Mock HttpServletResponse response;
    @Mock FilterChain         filterChain;

    @InjectMocks JwtAuthFilter filter;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sin header Authorization: continúa sin autenticación")
    void sinHeader_noAutentica() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Header sin Bearer: continúa sin autenticación")
    void headerSinBearer_noAutentica() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Token válido y validado: autentica al usuario")
    void tokenValido_autenticaUsuario() throws Exception {
        UserDetails userDetails = new User("juan@test.cl", "pass", List.of());

        when(request.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJIUzI1NiJ9.test");
        when(jwtUtil.extractEmail("eyJhbGciOiJIUzI1NiJ9.test")).thenReturn("juan@test.cl");
        when(userDetailsService.loadUserByUsername("juan@test.cl")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid(anyString(), eq(userDetails))).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("juan@test.cl");
    }

    @Test
    @DisplayName("Token presente pero isTokenValid=false: no autentica")
    void tokenInvalido_noAutentica() throws Exception {
        UserDetails userDetails = new User("juan@test.cl", "pass", List.of());

        when(request.getHeader("Authorization")).thenReturn("Bearer expiredtoken");
        when(jwtUtil.extractEmail("expiredtoken")).thenReturn("juan@test.cl");
        when(userDetailsService.loadUserByUsername("juan@test.cl")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid(anyString(), eq(userDetails))).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Excepción al parsear token: limpia contexto y continúa")
    void excepcionAlParsear_limpiContextoYContinua() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed");
        when(jwtUtil.extractEmail("malformed")).thenThrow(new RuntimeException("token inválido"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
