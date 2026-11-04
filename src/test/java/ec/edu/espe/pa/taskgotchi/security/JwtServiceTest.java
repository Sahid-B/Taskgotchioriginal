package ec.edu.espe.pa.taskgotchi.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "clave-de-pruebas-de-al-menos-32-caracteres";
    private final JwtService service = new JwtService(SECRET, 60);

    private String sign(String secret, JwtClaimsSet claims) {
        var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key))
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @Test
    void issuedTokenIdentifiesUser() {
        AuthenticatedUser user = service.parse(service.issueToken(7L, "ana"));
        assertThat(user).isEqualTo(new AuthenticatedUser(7L, "ana"));
    }

    @Test
    void rejectsTokenWithoutExpiration() {
        String token = sign(SECRET, JwtClaimsSet.builder().issuer(JwtService.ISSUER).subject("7").issuedAt(Instant.now()).build());
        assertThatThrownBy(() -> service.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredWrongIssuerAndForeignSignature() {
        Instant past = Instant.now().minusSeconds(3600);
        String expired = sign(SECRET, JwtClaimsSet.builder().issuer(JwtService.ISSUER).subject("7")
                .issuedAt(past.minusSeconds(60)).expiresAt(past).build());
        String otherIssuer = sign(SECRET, JwtClaimsSet.builder().issuer("otro").subject("7")
                .expiresAt(Instant.now().plusSeconds(600)).build());
        String otherKey = sign("otra-clave-distinta-de-al-menos-32-caracteres", JwtClaimsSet.builder()
                .issuer(JwtService.ISSUER).subject("7").expiresAt(Instant.now().plusSeconds(600)).build());

        for (String token : new String[]{expired, otherIssuer, otherKey}) {
            assertThatThrownBy(() -> service.parse(token)).isInstanceOf(JwtException.class);
        }
    }

    @Test
    void refusesShortSecret() {
        assertThatThrownBy(() -> new JwtService("corta", 60)).isInstanceOf(IllegalStateException.class);
    }
}
