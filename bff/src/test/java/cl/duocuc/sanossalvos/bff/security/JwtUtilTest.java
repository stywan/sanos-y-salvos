package cl.duocuc.sanossalvos.bff.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "clave_local_desarrollo_no_usar_en_produccion_para_tests";

    private JwtUtil jwtUtil;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
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
    @DisplayName("parseToken: extrae subject y claims correctamente")
    void parseToken_validToken_returnsCorrectClaims() {
        String token = buildToken("usuario@test.cl", 42L, "USER");

        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("usuario@test.cl");
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    @DisplayName("parseToken: lanza JwtException con token manipulado")
    void parseToken_tamperedToken_throwsJwtException() {
        String token = buildToken("usuario@test.cl", 1L, "USER");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwtUtil.parseToken(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("parseToken: lanza excepción con token vacío")
    void parseToken_emptyToken_throwsException() {
        assertThatThrownBy(() -> jwtUtil.parseToken(""))
                .isInstanceOf(Exception.class);
    }
}
