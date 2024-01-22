package nostr.si4n6r.core.impl;

import com.auth0.jwt.JWT;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.PublicKey;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static com.auth0.jwt.algorithms.Algorithm.HMAC256;

@Data
@Log
@ToString
@EqualsAndHashCode
public class Session {

    enum Status {
        NEW,
        ACTIVE,
        INACTIVE
    }

    private final PublicKey app;
    private final String id;
    private final String jwtToken;
    private Status status;
    @ToString.Exclude
    private final List<Request> requests;
    @ToString.Exclude
    private final List<Response> responses;

    Session(@NonNull PublicKey user, @NonNull PublicKey app, int timeout, String password, String secret) {
        log.log(Level.INFO, "Creating new session...");

        this.id = UUID.randomUUID().toString();
        this.status = Status.NEW;
        this.jwtToken = createToken(
                id,
                user,
                password,
                app,
                "si4n6r",
                secret,
                timeout);
        log.log(Level.INFO, "Created JWT: {0}", jwtToken);
        this.app = app;
        this.requests = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    public boolean hasExpired() {
        try {
            var jwt = JWT.decode(jwtToken);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error decoding JWT: {0}", e.getMessage());
            return true;
        }
    }
    private static String createToken(
            @NonNull String id,
            @NonNull PublicKey subject,
            @NonNull String password,
            @NonNull PublicKey audience,
            @NonNull String issuer,
            @NonNull String secret,
            int timeout)
    {
        return JWT.create()
                .withExpiresAt(Instant.now().plus(Duration.ofMinutes(timeout)))
                .withIssuedAt(Instant.now())
                .withIssuer(issuer)
                .withAudience(audience.toString())
                .withSubject(subject.toString())
                .withJWTId(id)
                .withClaim("password", password)
                .sign(HMAC256(secret));
    }

}
