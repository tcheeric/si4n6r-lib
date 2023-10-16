package nostr.si4n6r.signer.provider;

import lombok.extern.java.Log;
import nostr.api.NIP04;
import nostr.api.NIP46;
import nostr.base.Relay;
import nostr.si4n6r.core.Session;
import nostr.si4n6r.core.SessionManager;
import nostr.si4n6r.signer.Signer;
import nostr.si4n6r.signer.SignerService;
import nostr.si4n6r.util.Util;
import nostr.util.NostrException;
import nostr.ws.handler.command.spi.ICommandHandler;

import java.util.logging.Level;

import static nostr.api.Nostr.Json.decodeEvent;
import static nostr.si4n6r.util.Util.toRequest;


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
        // TODO - Refactor, ugly
        log.log(Level.FINE, "Recipient: {0} - Signer: {1}", new Object[]{recipient, signer.getIdentity().getPublicKey()});
        if (event.getKind() == 24133 && recipient.equals(signer.getIdentity().getPublicKey())) {
            log.log(Level.INFO, "Processing event {0}", event);

            String content;
            try {
                content = NIP04.decrypt(signer.getIdentity(), event);
            } catch (NostrException e) {
                throw new RuntimeException(e);
            }
            log.log(Level.FINE, "Content: {0}", content);

            if (content != null) {
                var nip46Request = NIP46.NIP46Request.fromString(content);
                var request = toRequest(nip46Request, app);
                var sessionManager = SessionManager.getInstance();
                var service = SignerService.getInstance();

                try {
                    sessionManager.addRequest(request, app);
                    service.handle(request);
                } catch (Session.SessionTimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            log.log(Level.FINE, "Skipping event {0} with nip {1}. All fine!", new Object[]{event, event.getNip()});
        }
    }

    @Override
    public void onAuth(String challenge, Relay relay) {
        log.log(Level.FINER, "onAuth({0}, {1})", new Object[]{challenge, relay});
    }
}