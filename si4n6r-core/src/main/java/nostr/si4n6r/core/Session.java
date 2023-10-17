package nostr.si4n6r.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.PublicKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@Data
@Log
@ToString
@EqualsAndHashCode
public class Session {

    private final static int FIVE_MINUTES = 60 * 5 * 1000;
    private final static int TEN_MINUTES = 60 * 10 * 1000;

    public final static int DEFAULT_INACTIVITY_TIMEOUT = FIVE_MINUTES;
    public final static int DEFAULT_DURATION = TEN_MINUTES;

    private String id;

    private final PublicKey publicKey;

    @ToString.Exclude
    private Date date;

    @ToString.Exclude
    private Date lastUpdate;

    private int inactivityTimeout;

    private int duration;

    @ToString.Exclude
    private final List<Request> requests;

    @ToString.Exclude
    private final List<Response> responses;

    private static Session instance;

    private Session(@NonNull PublicKey publicKey) {
        this(publicKey, FIVE_MINUTES, TEN_MINUTES);
    }

    private Session(@NonNull PublicKey publicKey, int timeout, int duration) {
        log.log(Level.INFO, "Creating new session...");
        this.id = UUID.randomUUID().toString();
        this.publicKey = publicKey;
        this.date = new Date();
        this.lastUpdate = new Date();
        this.requests = new ArrayList<>();
        this.responses = new ArrayList<>();
        this.inactivityTimeout = timeout;
        this.duration = duration;
    }

    public static Session getInstance(@NonNull PublicKey publicKey) {
        return getInstance(publicKey, FIVE_MINUTES, TEN_MINUTES);
    }

    public static Session getInstance(@NonNull PublicKey publicKey, int timeout, int duration) {
        var sessionManager = SessionManager.getInstance();
        if (instance == null || sessionManager.hasTimedOut(publicKey)) {
            instance = new Session(publicKey, timeout, duration);
        }
        return instance;
    }

    public static class SessionTimeoutException extends Exception {
        public SessionTimeoutException(@NonNull Session session) {
            super("Session " + session.getId() + " timed out!");
        }

        public SessionTimeoutException(@NonNull String message) {
            super(message);
        }
    }
}
