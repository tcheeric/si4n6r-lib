package nostr.si4n6r.util;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.tag.PubKeyTag;

public class Util {

    public static GenericEvent decodeEvent(@NonNull String jsonEvent) {
        var decoder = new GenericEventDecoder(jsonEvent);
        return decoder.decode();
    }

    public static PublicKey getEventRecipient(@NonNull GenericEvent nce) {
        var pubKeyTag = nce.getTags()
                .stream()
                .filter(t -> t.getCode().equals("p"))
                .findFirst()
                .orElse(null);

        assert (pubKeyTag != null);

        return ((PubKeyTag) pubKeyTag).getPublicKey();
    }

    public static PubKeyTag getUser(@NonNull GenericEvent event) {
        var tags = event.getTags();
        return (PubKeyTag) tags.stream()
                .filter(t -> t.getCode().equals("p"))
                .skip(1)
                .findFirst()
                .orElse(null);
    }

    public static String getAccountBaseDirectory() {
        return System.getProperty("user.home") + "/.si4n6r";
    }    
}
