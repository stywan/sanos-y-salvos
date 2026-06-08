package cl.duocuc.sanossalvos.geolocation.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "geo-test-secret-key-long-enough-for-hs256!!";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    private String buildToken(Long userId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("userId", userId, "role", "USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000L))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("parseToken: extrae claims de token válido")
    void parseToken_tokenValido_retornaClaims() {
        String token = buildToken(1L, "geo@test.cl");

        var claims = jwtUtil.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("geo@test.cl");
        assertThat(claims.get("userId", Long.class)).isEqualTo(1L);
    }

    @Test
    @DisplayName("parseToken: token inválido lanza excepción")
    void parseToken_tokenInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> jwtUtil.parseToken("esto.no.es.valido"))
                .isInstanceOf(Exception.class);
    }
}
