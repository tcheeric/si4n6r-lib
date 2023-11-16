package nostr.si4n6r.core.impl;

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
// TODO - Only create a new instance if the user's public key has been registered, else return throw an exception.
public class Session {

    private final static int FIVE_MINUTES = 60 * 5 * 1000;
    public final static int DEFAULT_INACTIVITY_TIMEOUT = FIVE_MINUTES;
    private final static int TEN_MINUTES = 60 * 10 * 1000;
    public final static int DEFAULT_DURATION = TEN_MINUTES;

    private static Session instance;
    private final PublicKey publicKey;

    @ToString.Exclude
    private final List<Request> requests;
    @ToString.Exclude
    private final List<Response> responses;
    private String id;
    @ToString.Exclude
    private Date date;
    @ToString.Exclude
    private Date lastUpdate;
    private int inactivityTimeout;
    private int duration;


    private Session(@NonNull PublicKey publicKey, int timeout, int duration, @NonNull String baseDirectory) throws SecurityManager.SecurityManagerException {
        log.log(Level.INFO, "Creating new session...");

        this.publicKey = publicKey;
        //validatePublicKeyRegistration(baseDirectory);

        validatePassword(publicKey);

        this.id = UUID.randomUUID().toString();
        this.date = new Date();
        this.lastUpdate = new Date();
        this.requests = new ArrayList<>();
        this.responses = new ArrayList<>();
        this.inactivityTimeout = timeout;
        this.duration = duration;
    }

    public static Session getInstance(@NonNull PublicKey publicKey) throws SecurityManager.SecurityManagerException {
        return getInstance(publicKey, FIVE_MINUTES, TEN_MINUTES, System.getProperty("user.home"));
    }

    public static Session getInstance(@NonNull PublicKey publicKey, int timeout, int duration, @NonNull String baseDirectory) throws SecurityManager.SecurityManagerException {
        var sessionManager = SessionManager.getInstance();
        if (instance == null || sessionManager.hasTimedOut(publicKey)) {
            instance = new Session(publicKey, timeout, duration, baseDirectory);
        }
        return instance;
    }

    public boolean hasTimedOut() {
        if (this.getInactivityTimeout() == -1 || this.getDuration() == -1) {
            return false;
        }

        return (new Date().getTime() - lastUpdate.getTime() > inactivityTimeout) ||
                (new Date().getTime() - this.getDate().getTime() > this.getDuration());
    }

    public static class SessionTimeoutException extends Exception {
        public SessionTimeoutException(@NonNull Session session) {
            super("Session " + session.getId() + " timed out!");
        }

        public SessionTimeoutException(@NonNull String message) {
            super(message);
        }
    }

    private static void validatePassword(PublicKey publicKey) throws SecurityManager.SecurityManagerException {
        SecurityManager securityManager = SecurityManager.getInstance();
        if (!securityManager.hasPrincipal(publicKey)) {
            throw new SecurityManager.SecurityManagerException(publicKey);
        }

        var principal = securityManager.getPrincipal(publicKey);
        if (principal.getPassword() == null) {
            throw new SecurityManager.SecurityManagerException("The public key " + publicKey + " has no password!");
        }
    }

}
