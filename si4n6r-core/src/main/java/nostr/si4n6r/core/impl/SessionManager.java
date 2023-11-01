package nostr.si4n6r.core.impl;

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

    public Session createSession(@NonNull PublicKey publicKey) throws SecurityManager.SecurityManagerException {
        var session = Session.getInstance(publicKey);
        addSession(session);
        return session;
    }

    public void addRequest(@NonNull Request request, @NonNull PublicKey publicKey) throws Session.SessionTimeoutException {

        var session = getSession(publicKey);

        if (session.hasTimedOut()) {
            throw new Session.SessionTimeoutException(session);
        }

        var requests = session.getRequests();

        if (requests.contains(request)) {
            log.log(Level.WARNING, "The request {0} is already in the session. Ignoring....");
            return;
        }

        log.log(Level.INFO, "Linking request {0} to session {1}", new Object[]{request, session.getId()});
        session.setLastUpdate(new Date());
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
        session.setLastUpdate(new Date());
        responses.add(response);
        response.setSessionId(session.getId());
    }

    public void invalidate(@NonNull PublicKey publicKey) {

        var session = getSession(publicKey);

        log.log(Level.INFO, "Invalidating session {0}", session.getId());
        session.setLastUpdate(new Date(0));
        session.setInactivityTimeout(0);
        SecurityManager.getInstance().removePrincipal(publicKey);
    }

    public boolean addSession(@NonNull Session session) {
        if (this.sessions.contains(session)) {
            return false;
        }
        this.sessions.add(session);
        return true;
    }

    public boolean addSession(@NonNull PublicKey publicKey) throws SecurityManager.SecurityManagerException {
        var session = Session.getInstance(publicKey);
        return addSession(session);
    }

    public boolean removeSession(@NonNull Session session) {
        if (!this.sessions.contains(session)) {
            return false;
        }
        if (session.hasTimedOut()) {
            this.sessions.remove(session);
            return true;
        }
        return false;
    }

    public boolean removeSession(@NonNull PublicKey publicKey) {
        try {
            var session = getSession(publicKey);
            return this.removeSession(session);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public Session getSession(@NonNull PublicKey publicKey) {
        return sessions.stream()
                .filter(session -> session.getPublicKey().equals(publicKey))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Session not found!"));
    }

    public boolean hasActiveSession(@NonNull PublicKey publicKey) {
        var optSession = sessions.stream()
                .filter(session -> session.getPublicKey().equals(publicKey) && !session.hasTimedOut())
                .findFirst();

        return optSession.isPresent();
    }


    boolean hasTimedOut(@NonNull PublicKey publicKey) {
        Session session;

        try {
            session = getSession(publicKey);
        } catch (RuntimeException e) {
            return true;
        }

        return session.hasTimedOut();
    }
}
