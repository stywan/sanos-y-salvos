package cl.duocuc.sanossalvos.petmanagement.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs256!!";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    private String buildToken(Long userId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("userId", userId, "roles", List.of("USER")))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000L))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("getEmail: extrae el subject del token JWT")
    void getEmail_extraeSubject() {
        String token = buildToken(42L, "usuario@test.cl");

        assertThat(jwtUtil.getEmail(token)).isEqualTo("usuario@test.cl");
    }

    @Test
    @DisplayName("getUserId: extrae el userId del claim")
    void getUserId_extraeUserId() {
        String token = buildToken(42L, "usuario@test.cl");

        assertThat(jwtUtil.getUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("isValid: retorna true para token correcto")
    void isValid_tokenCorrecto_returnsTrue() {
        String token = buildToken(1L, "a@b.cl");

        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("isValid: retorna false para token inválido")
    void isValid_tokenInvalido_returnsFalse() {
        assertThat(jwtUtil.isValid("esto.no.es.un.jwt")).isFalse();
    }
}
