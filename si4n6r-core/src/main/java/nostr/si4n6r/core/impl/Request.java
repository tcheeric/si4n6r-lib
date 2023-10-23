package nostr.si4n6r.core.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.si4n6r.core.IMethod;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Request {

    private final String id;
    private final PublicKey app;
    private final IMethod method;
    private String sessionId;

/*
    public Request(@NonNull IMethod method) {
        this(UUID.randomUUID().toString(), (PublicKey) method.getParameter("pubkey").get(), method, null);
    }
*/

    public Request(@NonNull IMethod method, @NonNull PublicKey app) {
        this(UUID.randomUUID().toString(), app, method, null);
    }
}
