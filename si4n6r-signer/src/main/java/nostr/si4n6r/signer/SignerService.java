package nostr.si4n6r.signer;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.NIP46;
import nostr.api.Nostr;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.GenericEvent;
import nostr.si4n6r.core.IMethod;
import nostr.si4n6r.core.impl.SecurityManager;
import nostr.si4n6r.core.impl.*;
import nostr.si4n6r.signer.methods.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static nostr.si4n6r.core.IMethod.Constants.*;

@Data
@Log
public class SignerService {

    private static SignerService instance;
    private final Signer signer;
    private final SessionManager sessionManager;

    private SignerService() {
        this.signer = Signer.getInstance();
        this.sessionManager = SessionManager.getInstance();
    }

    private SignerService(@NonNull Relay relay) {
        this.signer = Signer.getInstance(relay);
        this.sessionManager = SessionManager.getInstance();
    }

    public static SignerService getInstance() {
        if (instance == null) {
            instance = new SignerService();
        }
        return instance;
    }

    public static SignerService getInstance(@NonNull Relay relay) {
        if (instance == null) {
            instance = new SignerService(relay);
        }
        return instance;
    }

    /**
     * Signer-initiated connection to an application.
     *
     * @param app the application to connect to
     * @throws nostr.si4n6r.core.impl.SecurityManager.SecurityManagerException
     */
    public void doConnect(@NonNull ApplicationProxy app) throws SecurityManager.SecurityManagerException {
        final PublicKey appPublicKey = new PublicKey(app.getPublicKey());
        IMethod<String> connect = new Connect(appPublicKey);
        var request = new Request<>(connect, app);
        request.setSessionId(sessionManager.createSession(appPublicKey).getId());

        sessionManager.addRequest(request, appPublicKey);

        List<String> params = new ArrayList<>();
        params.add(app.toString());

        log.log(Level.INFO, "Submitting request {0}", request);
        var event = NIP46.createRequestEvent(new NIP46.NIP46Request(request.getId(), METHOD_CONNECT, params, request.getSessionId()), signer.getIdentity(), appPublicKey);

        Nostr.sign(signer.getIdentity(), event);
        Nostr.send(event);
    }

    // TODO - Does not need to be public. Package protected?
    /**
     * Handling app-initiated requests and submit a corresponding response back.
     *
     * @param request the request to handle and respond to.
     * @throws nostr.si4n6r.core.impl.SecurityManager.SecurityManagerException
     */
    public void handle(@NonNull Request request) throws SecurityManager.SecurityManagerException {

        log.log(Level.INFO, "Handling {0}", request);

        var app = request.getInitiator();
        var appPublicKey = new PublicKey(app.getPublicKey());
        var method = request.getMethod();

        if (!METHOD_CONNECT.equals(method.getName())) {
            validateSession(request, new PublicKey(app.getPublicKey()));
        }

        GenericEvent event = null;
        Response response = null;
        var sender = signer.getIdentity();

        switch (method.getName()) {
            case METHOD_DESCRIBE -> {
                if (method instanceof Describe describe && this.sessionManager.hasActiveSession(appPublicKey)) {
                    doDescribe(describe);
                    response = new Response(request.getId(), METHOD_DESCRIBE, describe.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_DESCRIBE, response.getResult().toString(), null, request.getSessionId()), sender, appPublicKey);
                }
            }
            case METHOD_DISCONNECT -> {
                if (method instanceof Disconnect disconnect && this.sessionManager.hasActiveSession(appPublicKey)) {
                    doDisconnect(disconnect, appPublicKey);
                    response = new Response(request.getId(), METHOD_DISCONNECT, disconnect.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_DISCONNECT, response.getResult().toString(), null, request.getSessionId()), sender, appPublicKey);
                }
            }
            case METHOD_CONNECT -> {
                if (method instanceof Connect connect && !this.sessionManager.hasActiveSession(appPublicKey)) {
                    doConnect(connect, appPublicKey);
                    response = new Response(request.getId(), METHOD_CONNECT, connect.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_CONNECT, response.getResult().toString(), null, request.getSessionId()), sender, appPublicKey);
                }
            }
            case METHOD_GET_PUBLIC_KEY -> {
                if (method instanceof GetPublicKey getPublicKey && this.sessionManager.hasActiveSession(appPublicKey)) {
                    getPublicKey.setResult(signer.getIdentity().getPublicKey());
                    response = new Response(request.getId(), METHOD_GET_PUBLIC_KEY, getPublicKey.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_GET_PUBLIC_KEY, response.getResult().toString(), null, request.getSessionId()), sender, appPublicKey);
                }
            }
            case METHOD_SIGN_EVENT -> {
                if (method instanceof SignEvent signEvent && this.sessionManager.hasActiveSession(appPublicKey)) {
                    IEvent paramEvent = (IEvent) signEvent.getParameter("pubkey").get();
                    Nostr.sign((ISignable) paramEvent);
                    signEvent.setResult(paramEvent);
                    response = new Response(request.getId(), METHOD_SIGN_EVENT, signEvent.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_SIGN_EVENT, response.getResult().toString(), null, request.getSessionId()), sender, appPublicKey);
                }
            }
            default ->
                throw new RuntimeException("Invalid request: " + request);
        }

        if (event == null) {
            throw new RuntimeException("Invalid request: " + request);
        }

        sessionManager.addResponse(response, appPublicKey);
        sessionManager.addRequest(request, appPublicKey);

        log.log(Level.INFO, "Submitting event {0}", event);

        Nostr.sign(sender, event);
        Nostr.send(event);

        cleanup(method, appPublicKey);
    }

    private void cleanup(@NonNull IMethod method, @NonNull PublicKey app) {
        if (METHOD_DISCONNECT.equals(method.getName())) {
            this.sessionManager.invalidate(app);
            //this.sessionManager.removeSession(this.sessionManager.getSession(app));
        }
    }

    private void validateSession(Request request, PublicKey app) {
        Session session = sessionManager.getSession(new PublicKey(request.getInitiator().getPublicKey()));
        boolean hasActiveSession = this.sessionManager.hasActiveSession(app);
        String sessionId = request.getSessionId();

        if (sessionId != null && !session.getId().equals(sessionId)) {
            throw new RuntimeException(String.format("Failed validation: Invalid session id %s for %s.", sessionId, app));
        }

        if (hasActiveSession && sessionId == null) {
            throw new RuntimeException(String.format("Failed validation: Missing session id for %s.", app));
        }
    }

    private void doDisconnect(@NonNull PublicKey app) {
        sessionManager.invalidate(app);
        log.log(Level.WARNING, "App {0} disconnected!", app);
    }

    // TODO - Add the additional methods as they get implemented.
    // TODO - For later: dynamically retrieve the names from the list of concrete classes implementing the IMethod interface
    private void doDescribe(@NonNull IMethod method) {
        List<String> result = new ArrayList<>();
        result.add(METHOD_DESCRIBE);
        result.add(METHOD_CONNECT);
        result.add(METHOD_DISCONNECT);        

        log.log(Level.INFO, "describe: {0}", result);
        ((Describe) method).setResult(result);
    }

    private void doConnect(@NonNull IMethod method, @NonNull PublicKey app) throws SecurityManager.SecurityManagerException {
        this.sessionManager.addSession(Session.getInstance(app));
        log.log(Level.INFO, "ACK: {0} connected!", app);
        ((Connect) method).setResult("ACK");
    }

    private void doDisconnect(@NonNull IMethod method, @NonNull PublicKey app) {
        this.sessionManager.invalidate(app);
        log.log(Level.INFO, "ACK: {0} disconnected!", app);
        ((Disconnect) method).setResult("ACK");
    }
}
