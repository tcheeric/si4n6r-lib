package nostr.si4n6r.signer.provider;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.NIP04;
import nostr.api.NIP46;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.GenericEvent;
import nostr.si4n6r.core.IMethod;
import nostr.si4n6r.core.impl.ApplicationProxy;
import nostr.si4n6r.core.impl.Request;
import nostr.si4n6r.core.impl.SessionManager;
import nostr.si4n6r.signer.Signer;
import nostr.si4n6r.signer.SignerService;
import nostr.si4n6r.signer.methods.*;
import nostr.si4n6r.util.Util;
import nostr.util.NostrException;
import nostr.ws.handler.command.spi.ICommandHandler;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import static nostr.api.Nostr.Json.decodeEvent;
import static nostr.si4n6r.core.IMethod.Constants.METHOD_CONNECT;

@Log
public class SignerCommandHandler implements ICommandHandler {

    @Override
    public void onEose(String subId, Relay relay) {
        log.log(Level.FINER, "onEose({0}, {1})", new Object[]{subId, relay});
        // TODO
    }

    @Override
    public void onOk(String eventId, String reasonMessage, Reason reason, boolean result, Relay relay) {
        log.log(Level.FINER, "onOk({0}, {1}, {2}, {3}, {4})", new Object[]{eventId, reasonMessage, reason, result, relay});
        // TODO
    }

    @Override
    public void onNotice(String message) {
        log.log(Level.FINER, "onNotice({0})", message);
        // TODO
    }

    @Override
    public void onEvent(String jsonEvent, String subId, Relay relay) {

        log.log(Level.FINE, "Received event {0} with subscription id {1} from relay {2}", new Object[]{jsonEvent, subId, relay});

        var event = decodeEvent(jsonEvent);
        log.log(Level.FINER, "Decoded event: {0}", event);

        // TODO
        var signer = Signer.getInstance();
        log.log(Level.FINE, "Signer: {0}", signer.getIdentity().getPublicKey());
        var app = event.getPubKey(); // Application.appIdentity
        var recipient = Util.getEventRecipient(event); // Signer

        // TODO - Also make sure the public key is a registered/known pubkey, and ignore all other pubkeys
        log.log(Level.FINE, "Recipient: {0} - Signer: {1}", new Object[]{recipient, signer.getIdentity().getPublicKey()});
        if (event.getKind() == 24133 && recipient.equals(signer.getIdentity().getPublicKey())) {
            handleKind24133(event, signer, app);
        } /*else if (event.getKind() == 4) {
            handleKind4(event, signer);
        } */ else {
            log.log(Level.FINE, "Skipping event {0} with nip {1}. All fine!", new Object[]{event, event.getNip()});
        }
    }

    @Override
    public void onAuth(String challenge, Relay relay) {
        log.log(Level.FINER, "onAuth({0}, {1})", new Object[]{challenge, relay});
    }

    private static Request toRequest(NIP46.NIP46Request nip46Request, @NonNull PublicKey application) {
        return new Request<>(
                nip46Request.getId(),
                new ApplicationProxy(application),
                toMethod(
                        nip46Request.getMethod(),
                        nip46Request.getParams()
                ),
                nip46Request.getSessionId(),
                new Date()
        );
    }

    private static IMethod toMethod(@NonNull String name, @NonNull List<String> params) {
        switch (name) {
            case METHOD_CONNECT -> {
                assert (params.size() == 1);
                var publicKey = getPublicKey(params.get(0));
                return new Connect(publicKey);
            }
            case IMethod.Constants.METHOD_DISCONNECT -> {
                assert (params.isEmpty());
                return new Disconnect();
            }
            case IMethod.Constants.METHOD_DESCRIBE -> {
                assert (params.isEmpty());
                return new Describe();
            }
            case IMethod.Constants.METHOD_GET_PUBLIC_KEY -> {
                assert (params.isEmpty());
                return new GetPublicKey();
            }
            case IMethod.Constants.METHOD_SIGN_EVENT -> {
                assert (params.size() == 1);
                var event = decodeEvent(params.get(0));
                return new SignEvent(event);
            }
            default -> throw new RuntimeException("Invalid method name " + name);
        }
    }

    private static PublicKey getPublicKey(@NonNull String hex) {
        return new PublicKey(hex);
    }

    private void handleKind24133(GenericEvent event, Signer signer, PublicKey app) throws RuntimeException {
        log.log(Level.INFO, "Processing event {0}", event);

        String content;
        try {
            content = NIP04.decrypt(signer.getIdentity(), event);
        } catch (NostrException e) {
            throw new RuntimeException(e);
        }
        log.log(Level.INFO, "Content: {0}", content);

        if (content != null) {
            var nip46Request = NIP46.NIP46Request.fromString(content);
            Request request = toRequest(nip46Request, app);
            var sessionManager = SessionManager.getInstance();

            log.log(Level.INFO, "Request: {0}", request);
            log.log(Level.INFO, "Method: {0}", request.getMethod().getName());

            sessionManager.addRequest(request, app);
            var service = SignerService.getInstance();
            service.handle(request);
        }
    }

}
