package cl.duocuc.sanossalvos.userauth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "clave_local_desarrollo_no_usar_en_produccion";
    private static final long   EXP_MS = 3_600_000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",       SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXP_MS);
    }

    private UserDetails ud(String email) {
        return new User(email, "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("generateToken + extractEmail: el subject coincide con el email")
    void generateToken_extractEmail_match() {
        String token = jwtUtil.generateToken(ud("ana@test.cl"), 5L, "PERSONA");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("ana@test.cl");
    }

    @Test
    @DisplayName("extractUserId: recupera el claim userId correctamente")
    void extractUserId_returnsCorrectId() {
        String token = jwtUtil.generateToken(ud("ana@test.cl"), 99L, "PERSONA");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(99L);
    }

    @Test
    @DisplayName("extractRoles: recupera la lista de roles del token")
    void extractRoles_returnsRoles() {
        String token = jwtUtil.generateToken(ud("ana@test.cl"), 1L, "PERSONA");
        assertThat(jwtUtil.extractRoles(token)).contains("ROLE_USER");
    }

    @Test
    @DisplayName("isTokenValid: true con usuario y token que coinciden")
    void isTokenValid_validToken_returnsTrue() {
        UserDetails userDetails = ud("ana@test.cl");
        String token = jwtUtil.generateToken(userDetails, 1L, "PERSONA");
        assertThat(jwtUtil.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: false cuando el email no coincide")
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtUtil.generateToken(ud("ana@test.cl"), 1L, "PERSONA");
        assertThat(jwtUtil.isTokenValid(token, ud("otro@test.cl"))).isFalse();
    }
}
