package nostr.si4n6r.core.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.si4n6r.core.IMethod;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Request<T> {

    private final String id;
    private final PublicKey app;
    private final IMethod<T> method;
    private String sessionId;
    private String password;

    public Request(@NonNull IMethod<T> method, @NonNull PublicKey app) {
        this(UUID.randomUUID().toString(), app, method, null, null);
    }
}
