package cl.duocuc.sanossalvos.notification.security;

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

    private static final String SECRET = "notif-test-secret-key-long-enough-for-hs256!!";
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
    @DisplayName("parseToken: extrae subject y claims de token válido")
    void parseToken_valido_retornaClaims() {
        String token = buildToken(5L, "notif@test.cl");
        var claims = jwtUtil.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("notif@test.cl");
        assertThat(claims.get("userId", Long.class)).isEqualTo(5L);
    }

    @Test
    @DisplayName("parseToken: token inválido lanza excepción")
    void parseToken_invalido_lanzaExcepcion() {
        assertThatThrownBy(() -> jwtUtil.parseToken("esto.no.es.valido"))
                .isInstanceOf(Exception.class);
    }
}
