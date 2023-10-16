package nostr.si4n6r.core;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PublicKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

@Data
@Log
public class SessionManager {

    private final List<Session> sessions;

    private static SessionManager instance;

    private SessionManager() {
        this.sessions = new ArrayList<>();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void addRequest(@NonNull Request request, @NonNull PublicKey publicKey) throws Session.SessionTimeoutException {
        checkTimeout(publicKey);

        var session = getSession(publicKey);
        var requests = session.getRequests();

        if (requests.contains(request)) {
            return;
        }

        log.log(Level.FINER, "Adding request {0} to session {1}", new Object[]{request, session.getId()});
        session.setLastUpdate(new Date());
        requests.add(request);
    }

    public void addResponse(@NonNull Response response, @NonNull PublicKey publicKey) throws Session.SessionTimeoutException {
        checkTimeout(publicKey);

        var session = getSession(publicKey);
        var responses = session.getResponses();

        if (responses.contains(response)) {
            return;
        }

        log.log(Level.FINER, "Adding response {0} to session {1}", new Object[]{response, session.getId()});
        session.setLastUpdate(new Date());
        responses.add(response);
    }

    public void invalidate(@NonNull PublicKey publicKey) {

        var session = getSession(publicKey);

        log.log(Level.INFO, "Invalidating session {0}", session.getId());
        session.setLastUpdate(new Date(0));
        session.setInactivityTimeout(0);
    }

    public boolean addSession(@NonNull Session session) {
        if (this.sessions.contains(session)) {
            return false;
        }
        this.sessions.add(session);
        return true;
    }

    public boolean removeSession(@NonNull Session session) {
        if (!this.sessions.contains(session)) {
            return false;
        }
        if (hasTimedOut(session.getPublicKey())) {
            this.sessions.remove(session);
            return true;
        }
        return false;
    }

    public Session getSession(@NonNull PublicKey publicKey) {
        return sessions.stream()
                .filter(session -> session.getPublicKey().equals(publicKey))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Session not found!"));
    }

    boolean hasTimedOut(@NonNull PublicKey publicKey) {

        Session session;

        try {
            session = getSession(publicKey);
        } catch (RuntimeException e) {
            return true;
        }

        if (session.getInactivityTimeout() == -1 || session.getDuration() == -1) {
            return false;
        }

        return (new Date().getTime() - session.getLastUpdate().getTime() > session.getInactivityTimeout()) ||
                (new Date().getTime() - session.getDate().getTime() > session.getDuration());
    }

    private void checkTimeout(@NonNull PublicKey publicKey) throws Session.SessionTimeoutException {

        var session = getSession(publicKey);

        if (session.getLastUpdate() == null) {
            return;
        }
        if (hasTimedOut(publicKey)) {
            throw new Session.SessionTimeoutException(session);
        }
    }
}
