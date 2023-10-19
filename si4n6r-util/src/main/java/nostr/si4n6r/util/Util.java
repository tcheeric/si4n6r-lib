package nostr.si4n6r.util;

import lombok.NonNull;
import nostr.api.NIP46;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.tag.PubKeyTag;
import nostr.si4n6r.core.IMethod;
import nostr.si4n6r.core.Request;
import nostr.si4n6r.core.Response;
import nostr.si4n6r.core.impl.*;

import java.util.List;

import static nostr.si4n6r.core.IMethod.Constants.*;

public class Util {

    public static Request toRequest(NIP46.NIP46Request nip46Request, @NonNull PublicKey application) {
        return new Request(
                nip46Request.getId(),
                application,
                toMethod(
                        nip46Request.getMethod(),
                        nip46Request.getParams()
                ),
                nip46Request.getSessionId()
        );
    }

    public static Response toResponse(@NonNull NIP46.NIP46Response nip46Response) {
        Response response = new Response(nip46Response.getId(), nip46Response.getMethod(), nip46Response.getResult());
        response.setError(nip46Response.getError());
        return response;
    }

    public static IMethod toMethod(@NonNull String name, @NonNull List<String> params) {
        switch (name) {
            case METHOD_CONNECT -> {
                assert (params.size() == 1);
                var publicKey = getPublicKey(params.get(0));
                return new Connect(publicKey);
            }
            case METHOD_DISCONNECT -> {
                assert (params.isEmpty());
                return new Disconnect();
            }
            case METHOD_DESCRIBE -> {
                assert (params.isEmpty());
                return new Describe();
            }
            case METHOD_GET_PUBLIC_KEY -> {
                assert (params.size() == 0);
                return new GetPublicKey();
            }
            case METHOD_SIGN_EVENT -> {
                assert (params.size() == 1);
                var event = decodeEvent(params.get(0));
                return new SignEvent(event);
            }
            default -> throw new RuntimeException("Invalid method name " + name);
        }
    }

    public static PublicKey getPublicKey(@NonNull String hex) {
        return new PublicKey(hex);
    }

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
}
