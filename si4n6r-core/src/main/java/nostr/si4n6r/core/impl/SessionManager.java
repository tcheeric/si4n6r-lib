package nostr.si4n6r.core.impl;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PublicKey;

import java.util.ArrayList;
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

    public Session createSession(
            @NonNull PublicKey user,
            @NonNull PublicKey app,
            int timeout,
            @NonNull String password) {
        var session = new Session(user, app, timeout, password);
        addSession(session);
        return session;
    }

    public void addRequest(@NonNull Request request, @NonNull PublicKey publicKey) {

        var session = getSession(publicKey);
        var requests = session.getRequests();

        if (requests.contains(request)) {
            log.log(Level.WARNING, "The request {0} is already in the session. Ignoring....");
            return;
        }

        log.log(Level.FINE, "Linking request {0} to session {1}", new Object[]{request, session.getId()});
        requests.add(request);
        request.setSessionId(session.getId());
    }

    public void addResponse(@NonNull Response response, @NonNull PublicKey publicKey) {

        var session = getSession(publicKey);
        var responses = session.getResponses();

        if (responses.contains(response)) {
            return;
        }

        log.log(Level.FINER, "Adding response {0} to session {1}", new Object[]{response, session.getId()});
        responses.add(response);
        response.setSessionId(session.getId());
    }

    // TODO - Instead of removing the session, introduce a status (VALID, INVALID) and set it to INVALID
/*
    public void invalidate(@NonNull PublicKey publicKey) {

        var session = getSession(publicKey);
        session.setStatus(Session.Status.INACTIVE);
        //removeSession(session);
    }
*/

    public boolean addSession(@NonNull Session session) {
        if (this.sessions.contains(session)) {
            return false;
        }
        this.sessions.add(session);
        return true;
    }

    public void activateSession(@NonNull PublicKey publicKey) {
        var session = getSession(publicKey);
        session.setStatus(Session.Status.ACTIVE);
    }

    public void deactivateSession(@NonNull PublicKey publicKey) {
        var session = getSession(publicKey);
        session.setStatus(Session.Status.INACTIVE);
    }

    public boolean sessionIsActive(@NonNull PublicKey publicKey) {
        var session = getSession(publicKey);
        return session.getStatus().equals(Session.Status.ACTIVE);
    }

    public boolean sessionIsInactive(@NonNull PublicKey publicKey) {
        var session = getSession(publicKey);
        return session.getStatus().equals(Session.Status.INACTIVE);
    }

    public boolean sessionIsNew(@NonNull PublicKey publicKey) {
        var session = getSession(publicKey);
        return session.getStatus().equals(Session.Status.NEW);
    }

/*
    public boolean addSession(@NonNull PublicKey publicKey) throws SecurityManager.SecurityManagerException {
        var session = Session.getInstance(publicKey);
        return addSession(session);
    }

    public boolean removeSession(@NonNull Session session) {
        if (!this.sessions.contains(session)) {
            return false;
        }
        this.sessions.remove(session);
        return true;
    }

    public boolean removeSession(@NonNull PublicKey publicKey) {
        try {
            var session = getSession(publicKey);
            return this.removeSession(session);
        } catch (RuntimeException ex) {
            return false;
        }
    }
*/

    public Session getSession(@NonNull PublicKey publicKey) {
        return sessions.stream()
                .filter(session -> session.getApp().equals(publicKey))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Session not found!"));
    }

/*
    public boolean hasActiveSession(@NonNull PublicKey publicKey) {
        var optSession = sessions.stream()
                .filter(session -> session.getApp().equals(publicKey))
                .findFirst();

        return optSession.isPresent() && optSession.get().getStatus().equals(Session.Status.ACTIVE);
    }
*/

    boolean hasExpired(@NonNull PublicKey publicKey) {
        Session session;

        try {
            session = getSession(publicKey);
        } catch (RuntimeException e) {
            return true;
        }

        return session.hasExpired();
    }
}
