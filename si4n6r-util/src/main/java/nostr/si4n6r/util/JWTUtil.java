package nostr.si4n6r.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;

import static com.auth0.jwt.algorithms.Algorithm.HMAC256;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@AllArgsConstructor
@Getter
public class JWTUtil {

    private final String token;

    public static String createToken(
            @NonNull String sessionId,
            @NonNull String account,
            @NonNull String app,
            @NonNull String password,
            int timeout) {
        return JWT.create()
                .withExpiresAt(Instant.now().plus(Duration.ofMinutes(timeout)))
                .withIssuedAt(Instant.now())
                .withIssuer("si4n6r")
                .withAudience(app)
                .withSubject(account)
                .withJWTId(sessionId)
                .withClaim("password", password)
                .sign(Algorithm.HMAC512(getSecret()));
    }

    public boolean hasExpired() {
        try {
            var jwt = JWT.decode(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getSubject() {
        return decodedJWT().getSubject();
    }

    public String getIssuer() {
        return decodedJWT().getIssuer();
    }

    public String getAudience() {
        return decodedJWT().getAudience().get(0);
    }

    public String getPassword() {
        return getClaim("password");
    }

    public String getClaim(String name) {
        return decodedJWT().getClaim(name).asString();
    }

    private DecodedJWT decodedJWT() {
        var algorithm = Algorithm.HMAC512(getSecret());
        DecodedJWT jwt = JWT.require(algorithm)
                .build()
                .verify(token);
        return jwt;
    }

    private static String getSecret() {
        var prop = new Properties();

        try {
            var fis = JWTUtil.class.getClassLoader().getResourceAsStream("jwt.properties");
            if (fis == null) {
                throw new FileNotFoundException("jwt.properties not found in the classpath");
            }
            prop.load(fis);
            return prop.getProperty("secret");
        } catch (IOException var2) {
            throw new RuntimeException("Failed to read secret from jwt.properties", var2);
        }
    }
}
