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
import nostr.id.Identity;
import nostr.si4n6r.core.*;
import nostr.si4n6r.core.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static nostr.si4n6r.core.IMethod.Constants.*;

@Data
@Log
public class SignerService {

    private final Signer signer;
    private final ConnectionManager connectionManager;
    private final SessionManager sessionManager;

    private static SignerService instance;

    private SignerService() {
        this.signer = Signer.getInstance();
        this.connectionManager = ConnectionManager.getInstance();
        this.sessionManager = SessionManager.getInstance();
    }

    public static SignerService getInstance() {
        if (instance == null) {
            instance = new SignerService();
        }
        return instance;
    }

    // TODO - Does not need to be public. Package protected?
    public void handle(@NonNull Request request) {

        log.log(Level.INFO, "Handling {0}", request);

        var method = request.getMethod();
        var session = sessionManager.getSession(request.getApp());
        var app = request.getApp();

        if (null != request.getSessionId() && !session.getId().equals(request.getSessionId())) {
            if (this.connectionManager.isConnected(app)) {
                log.log(Level.WARNING, "Invalid session id {0} for {1}. Disconnecting...", new Object[]{request.getSessionId(), app});
                disconnect(app, session);
            }
        }

        if(this.connectionManager.isConnected(app) && null == request.getSessionId()) {
            log.log(Level.WARNING, "Invalid session id {0} for {1}. Disconnecting...", new Object[]{request.getSessionId(), app});
            disconnect(app, session);
        }

        GenericEvent event = null;
        Response response = null;
        var sender = signer.getIdentity();

        switch (method.getName()) {
            case METHOD_DESCRIBE -> {
                if (method instanceof Describe describe && this.connectionManager.isConnected(app)) {
                    describe(describe);
                    response = new Response(request.getId(), METHOD_DESCRIBE, describe.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), IMethod.Constants.METHOD_DESCRIBE, response.getResult().toString()), sender, app);
                }
            }
            case METHOD_DISCONNECT -> {
                if (method instanceof Disconnect disconnect && this.connectionManager.isConnected(app)) {
                    disconnect(disconnect, app);
                    response = new Response(request.getId(), METHOD_CONNECT, disconnect.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_DISCONNECT, response.getResult().toString()), sender, app);
                }
            }
            case METHOD_CONNECT -> {
                if (method instanceof Connect connect && this.connectionManager.isDisconnected(app)) {
                    connect(connect, app);
                    response = new Response(request.getId(), METHOD_CONNECT, connect.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_CONNECT, response.getResult().toString()), sender, app);
                }
            }
            case METHOD_GET_PUBLIC_KEY -> {
                if (method instanceof GetPublicKey getPublicKey && this.connectionManager.isConnected(app)) {
                    getPublicKey.setResult(Identity.getInstance().getPublicKey());
                    response = new Response(request.getId(), METHOD_GET_PUBLIC_KEY, getPublicKey.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_GET_PUBLIC_KEY, IMethod.Constants.METHOD_DESCRIBE, response.getResult().toString(), response.getSessionId()), sender, app);
                }
            }
            case METHOD_SIGN_EVENT -> {
                if (method instanceof SignEvent signEvent && this.connectionManager.isConnected(app)) {
                    IEvent paramEvent = (IEvent) signEvent.getParameter("pubkey");
                    Nostr.sign((ISignable) paramEvent);
                    signEvent.setResult(paramEvent);
                    response = new Response(request.getId(), METHOD_SIGN_EVENT, signEvent.getResult());
                    event = NIP46.createResponseEvent(new NIP46.NIP46Response(response.getId(), METHOD_SIGN_EVENT, IMethod.Constants.METHOD_DESCRIBE, response.getResult().toString(), response.getSessionId()), sender, app);
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
        this.connectionManager.disconnect(app);
        sessionManager.invalidate(app);
        throw new RuntimeException(new Session.SessionTimeoutException(session));
    }

    // TODO - Add more methods as they get implemented.
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
        if (method instanceof Connect connect && this.connectionManager.isDisconnected(app)) {
            this.connectionManager.connect(app);
            log.log(Level.INFO, "ACK: {0} connected!", app);
            connect.setResult("ACK");
            return;
        }
        throw new RuntimeException("Invalid method " + method);
    }

    private void disconnect(@NonNull IMethod method, @NonNull PublicKey app) {
        if (method instanceof Disconnect disconnect && this.connectionManager.isConnected(app)) {
            this.connectionManager.disconnect(app);
            log.log(Level.INFO, "ACK: {0} disconnected!", app);
            disconnect.setResult("ACK");
            return;
        }
        throw new RuntimeException("Invalid method " + method);
    }
}
