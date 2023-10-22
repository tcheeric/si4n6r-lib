package nostr.si4n6r.signer;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.NIP46;
import nostr.api.Nostr;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.si4n6r.core.IMethod;
import nostr.si4n6r.core.impl.Request;
import nostr.si4n6r.core.impl.Response;
import nostr.si4n6r.core.impl.Session;
import nostr.si4n6r.core.impl.SessionManager;
import nostr.si4n6r.core.impl.methods.Connect;
import nostr.si4n6r.core.impl.methods.Describe;
import nostr.si4n6r.core.impl.methods.Disconnect;
import nostr.si4n6r.core.impl.methods.GetPublicKey;
import nostr.si4n6r.core.impl.methods.SignEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static nostr.si4n6r.core.IMethod.Constants.METHOD_CONNECT;
import static nostr.si4n6r.core.IMethod.Constants.METHOD_DESCRIBE;
import static nostr.si4n6r.core.IMethod.Constants.METHOD_DISCONNECT;
import static nostr.si4n6r.core.IMethod.Constants.METHOD_GET_PUBLIC_KEY;
import static nostr.si4n6r.core.IMethod.Constants.METHOD_SIGN_EVENT;

@Data
@Log
public class SignerService {

    private final Signer signer;
    //private final ConnectionManager connectionManager;
    private final SessionManager sessionManager;

    private static SignerService instance;

    private SignerService() {
        this.signer = Signer.getInstance();
        //this.connectionManager = ConnectionManager.getInstance();
        this.sessionManager = SessionManager.getInstance();
    }

    public static SignerService getInstance() {
        if (instance == null) {
            instance = new SignerService();
        }
        return instance;
    }

    /**
     * Signer-initiated connection to an application.
     * @param app the application to connect to
     */
    public void connect(@NonNull PublicKey app) {
        IMethod<String> connect = new Connect(app);
        var request = new Request(connect, app);
        request.setSessionId(sessionManager.createSession(app).getId());

        List<String> params = new ArrayList<>();
        params.add(app.toString());

        log.log(Level.INFO, "Submitting request {0}", request);
        var event = NIP46.createRequestEvent(new NIP46.NIP46Request(request.getId(), METHOD_CONNECT, params, request.getSessionId()), signer.getIdentity(), app);

        Nostr.sign(signer.getIdentity(), event);
        Nostr.send(event);
    }

    // TODO - Does not need to be public. Package protected?

    /**
     * Handling app-initiated requests and submit a corresponding response back.
     * @param request the request to handle and respond to.
     */
    public void handle(@NonNull Request request) {

        log.log(Level.INFO, "Handling {0}", request);

        var method = request.getMethod();
        var session = sessionManager.getSession(request.getApp());
        var app = request.getApp();

        if (null != request.getSessionId() && !session.getId().equals(request.getSessionId())) {
            if (this.sessionManager.hasActiveSession(app)) {
                log.log(Level.WARNING, "Invalid session id {0} for {1}. Disconnecting...", new Object[]{request.getSessionId(), app});
                disconnect(app, session);
            }
        }

        if (this.sessionManager.hasActiveSession(app) && null == request.getSessionId()) {
            log.log(Level.WARNING, "Invalid session id {0} for {1}. Disconnecting...", new Object[]{request.getSessionId(), app});
            disconnect(app, session);
        }

        GenericEvent event = null;
        Response response = null;
        var sender = signer.getIdentity();

        switch (method.getName()) {
            case METHOD_DESCRIBE -> {
                if (method instanceof Describe describe && this.sessionManager.hasActiveSession(app)) {
                    describe(describe);
                    response = new Response(request.getId(), METHOD_DESCRIBE, describe.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_DESCRIBE, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            case METHOD_DISCONNECT -> {
                if (method instanceof Disconnect disconnect && this.sessionManager.hasActiveSession(app)) {
                    disconnect(disconnect, app);
                    response = new Response(request.getId(), METHOD_CONNECT, disconnect.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_DISCONNECT, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            case METHOD_CONNECT -> {
                if (method instanceof Connect connect && this.sessionManager.hasActiveSession(app)) {
                    connect(connect, app);
                    response = new Response(request.getId(), METHOD_CONNECT, connect.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_CONNECT, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            case METHOD_GET_PUBLIC_KEY -> {
                if (method instanceof GetPublicKey getPublicKey && this.sessionManager.hasActiveSession(app)) {
                    getPublicKey.setResult(signer.getIdentity().getPublicKey());
                    response = new Response(request.getId(), METHOD_GET_PUBLIC_KEY, getPublicKey.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_GET_PUBLIC_KEY, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            case METHOD_SIGN_EVENT -> {
                if (method instanceof SignEvent signEvent && this.sessionManager.hasActiveSession(app)) {
                    IEvent paramEvent = (IEvent) signEvent.getParameter("pubkey").get();
                    Nostr.sign((ISignable) paramEvent);
                    signEvent.setResult(paramEvent);
                    response = new Response(request.getId(), METHOD_SIGN_EVENT, signEvent.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_SIGN_EVENT, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            default -> throw new RuntimeException("Invalid request: " + request);
        }

        assert event != null;

        try {
            sessionManager.addResponse(response, app);
        } catch (Session.SessionTimeoutException e) {
            disconnect(app, session);
        }

        log.log(Level.INFO, "Submitting event {0}", event);

        Nostr.sign(sender, event);
        Nostr.send(event);
    }

    private void disconnect(@NonNull PublicKey app, @NonNull Session session) {
        sessionManager.invalidate(app);
        throw new RuntimeException(new Session.SessionTimeoutException(session));
    }

    // TODO - Add the additional methods as they get implemented.
    // TODO - For later: dynamically retrieve the names from the list of concrete classes implementing the IMethod interface
    private void describe(@NonNull IMethod method) {
        if (method instanceof Describe describe) {
            List<String> result = new ArrayList<>();
            result.add(METHOD_DESCRIBE);
            result.add(METHOD_CONNECT);
            result.add(METHOD_DISCONNECT);

            log.log(Level.INFO, "describe: {0}", result);
            describe.setResult(result);
        }
    }

    private void connect(@NonNull IMethod method, @NonNull PublicKey app) {
        if (method instanceof Connect connect && !this.sessionManager.hasActiveSession(app)) {
            this.sessionManager.addSession(Session.getInstance(app));
            log.log(Level.INFO, "ACK: {0} connected!", app);
            connect.setResult("ACK");
            return;
        }
        throw new RuntimeException("Invalid method " + method);
    }

    private void disconnect(@NonNull IMethod method, @NonNull PublicKey app) {
        if (method instanceof Disconnect disconnect && this.sessionManager.hasActiveSession(app)) {
            this.sessionManager.removeSession(Session.getInstance(app));
            log.log(Level.INFO, "ACK: {0} disconnected!", app);
            disconnect.setResult("ACK");
            return;
        }
        throw new RuntimeException("Invalid method " + method);
    }
}
