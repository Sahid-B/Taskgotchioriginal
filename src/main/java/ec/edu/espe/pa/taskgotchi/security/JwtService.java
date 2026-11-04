package ec.edu.espe.pa.taskgotchi.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

/**
 * Emite y valida tokens JWT firmados con HMAC-SHA256.
 * La clave se toma de JWT_SECRET (mínimo 32 caracteres). Si no se define, se genera
 * una aleatoria en cada arranque: sirve para desarrollo, pero los tokens dejan de
 * valer al reiniciar el backend.
 */
@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    static final String ISSUER = "taskgotchi";
    private static final int MIN_SECRET_BYTES = 32;

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final Duration expiration;

    public JwtService(@Value("${taskgotchi.security.jwt.secret:}") String secret,
                      @Value("${taskgotchi.security.jwt.expiration-minutes:120}") long expirationMinutes) {
        SecretKey key = new SecretKeySpec(resolveSecret(secret), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        NimbusJwtDecoder nimbusDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        // Expiración obligatoria: un token firmado sin "exp" no se acepta.
        JwtTimestampValidator timestamps = new JwtTimestampValidator();
        timestamps.setAllowEmptyExpiryClaim(false);
        nimbusDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamps, new JwtIssuerValidator(ISSUER)));
        this.decoder = nimbusDecoder;
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    private static byte[] resolveSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            log.warn("JWT_SECRET no está definido: se usa una clave temporal y las sesiones se pierden al reiniciar.");
            byte[] random = new byte[MIN_SECRET_BYTES];
            new SecureRandom().nextBytes(random);
            return random;
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos " + MIN_SECRET_BYTES + " caracteres");
        }
        return bytes;
    }

    public String issueToken(Long userId, String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /** Valida firma, emisor y expiración. Lanza {@link JwtException} si el token no es válido. */
    public AuthenticatedUser parse(String token) {
        Jwt jwt = decoder.decode(token);
        try {
            return new AuthenticatedUser(Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("username"));
        } catch (NumberFormatException e) {
            throw new BadJwtException("El token no identifica a un usuario");
        }
    }
}
