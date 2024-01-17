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
import nostr.si4n6r.core.impl.*;
import nostr.si4n6r.signer.methods.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
     */
    public void doConnect(@NonNull ApplicationProxy app) {
/*
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
*/
    }

    /**
     * Handling app-initiated requests and submit a corresponding response back.
     *
     * @param request the request to handle and respond to.
     */
    public void handle(@NonNull Request request) {

        var method = request.getMethod();
        var appProxy = request.getInitiator();

        log.log(Level.INFO, "Handling {0}", request);

        validateSession(request);

        GenericEvent event = null;
        Response response = null;
        var sender = signer.getIdentity();
        var app = new PublicKey(appProxy.getPublicKey());

        switch (method.getName()) {
            case METHOD_DESCRIBE -> {
                if (method instanceof Describe describe) {
                    doDescribe(describe, app);
                    response = new Response(request.getId(), METHOD_DESCRIBE, describe.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_DESCRIBE, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            case METHOD_DISCONNECT -> {
                if (method instanceof Disconnect disconnect) {
                    doDisconnect(disconnect, app);
                    response = new Response(request.getId(), METHOD_DISCONNECT, disconnect.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_DISCONNECT, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            case METHOD_CONNECT -> {
                if (method instanceof Connect connect) {
                    doConnect(connect, app);
                    response = new Response(request.getId(), METHOD_CONNECT, connect.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_CONNECT, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            case METHOD_GET_PUBLIC_KEY -> {
                if (method instanceof GetPublicKey getPublicKey) {
                    getPublicKey.setResult(signer.getIdentity().getPublicKey());
                    response = new Response(request.getId(), METHOD_GET_PUBLIC_KEY, getPublicKey.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_GET_PUBLIC_KEY, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            case METHOD_SIGN_EVENT -> {
                if (method instanceof SignEvent signEvent) {
                    IEvent paramEvent = (IEvent) signEvent.getParameter("pubkey").get();
                    Nostr.sign((ISignable) paramEvent);
                    signEvent.setResult(paramEvent);
                    response = new Response(request.getId(), METHOD_SIGN_EVENT, signEvent.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_SIGN_EVENT, response.getResult().toString(), null, request.getSessionId()), sender, app);
                }
            }
            default -> throw new RuntimeException("Invalid request: " + request);
        }

        if (event == null) {
            throw new RuntimeException("Invalid request: " + request);
        }

        sessionManager.addResponse(response, app);
        sessionManager.addRequest(request, app);

        log.log(Level.FINE, "Submitting event {0} to relay(s)", event);
        Nostr.sign(sender, event);
        Nostr.send(event);
    }

    private void validateSession(Request request) {
        var app = new PublicKey(request.getInitiator().getPublicKey());
        var session = sessionManager.getSession(app);
        var hasActiveSession = this.sessionManager.sessionIsActive(app);
        var sessionId = request.getSessionId();

        if (sessionId != null && !session.getId().equals(sessionId)) {
            throw new RuntimeException(String.format("Failed validation: Invalid session id %s for %s.", sessionId, app));
        }

        if (hasActiveSession && sessionId == null) {
            throw new RuntimeException(String.format("Failed validation: Missing session id for %s.", app));
        }
    }

    // TODO - Add the additional methods as they get implemented.
    // TODO - For later: dynamically retrieve the names from the list of concrete classes implementing the IMethod interface
    private void doDescribe(@NonNull IMethod method, @NonNull PublicKey app) {
        if (!isConnected(app)) {
            throw new RuntimeException(String.format("Failed to describe: %s is not connected!", app));
        }

        List<String> result = new ArrayList<>();
        result.add(METHOD_DESCRIBE);
        result.add(METHOD_CONNECT);
        result.add(METHOD_DISCONNECT);

        log.log(Level.INFO, "describe: {0}", result);
        ((Describe) method).setResult(result);
    }

    private void doConnect(@NonNull IMethod method, @NonNull PublicKey app) {
        if (isConnected(app)) {
            throw new RuntimeException(String.format("Failed to connect: %s is already connected!", app));
        }

        this.sessionManager.activateSession(app);
        log.log(Level.INFO, "ACK: {0} connected!", app);
        ((Connect) method).setResult("ACK");

    }

    private void doDisconnect(@NonNull IMethod method, @NonNull PublicKey app) {
        if (!isConnected(app)) {
            throw new RuntimeException(String.format("Failed to disconnect: %s is not connected!", app));
        }
        this.sessionManager.deactivateSession(app);
        log.log(Level.INFO, "ACK: {0} disconnected!", app);
        ((Disconnect) method).setResult("ACK");
    }

    // TODO - Implement the method. Check is a connection already exists for the app.
    private boolean isConnected(@NonNull PublicKey app) {
        var session = getSession(app);

        var request = getMostRecentConnectRequest(session);
        if (!request.isPresent()) {
            return false;
        }

        var response = getResponseForRequest(request);

        return response != null && response.getResult().equals("ACK");
    }

    private Optional<Request> getMostRecentConnectRequest(@NonNull Session session) {
        var requests = session.getRequests();

        return requests.stream()
                .filter(request -> request.getMethod() instanceof Connect)
                .max(Comparator.comparing(Request::getTimestamp));
    }

    private Response getResponseForRequest(Optional<Request> requestOptional) {
        if (!requestOptional.isPresent()) {
            return null; // or throw an exception
        }

        var request = requestOptional.get();
        var session = sessionManager.getSession(new PublicKey(request.getInitiator().getPublicKey()));
        var responses = session.getResponses();
        var responseOptional = responses.stream()
                .filter(response -> response.getId().equals(request.getId()))
                .findFirst();

        if (responseOptional.isPresent()) {
            Response response = responseOptional.get();
            if (request.getTimestamp().after(response.getTimestamp())) {
                throw new RuntimeException("Request was emitted after the response");
            }
            return response;
        }

        return null;
    }

    private Session getSession(PublicKey appPublicKey) {
        var sessionManager = SessionManager.getInstance();
        return sessionManager.getSession(appPublicKey);
    }

}
