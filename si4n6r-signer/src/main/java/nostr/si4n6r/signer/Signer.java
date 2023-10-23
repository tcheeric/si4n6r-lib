package nostr.si4n6r.signer;

import lombok.Data;
import lombok.NonNull;
import nostr.base.Relay;
import nostr.client.Client;
import nostr.id.CustomIdentity;

@Data
public class Signer {

    private static Signer instance;
    private final Relay relay;
    private final CustomIdentity identity;

    private Signer() {
        this.relay = Client.getInstance().getDefaultRelay();
        this.identity = new CustomIdentity("signer");
    }

    private Signer(@NonNull Relay relay) {
        this.relay = relay;
        this.identity = new CustomIdentity("signer");
    }

    public static Signer getInstance() {
        if (instance == null) {
            instance = new Signer();
        }
        return instance;
    }

    public static Signer getInstance(@NonNull Relay relay) {
        if (instance == null) {
            instance = new Signer(relay);
        }
        return instance;
    }
}
