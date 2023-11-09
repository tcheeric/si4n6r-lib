package nostr.si4n6r.core.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.si4n6r.core.IMethod;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Request<T, U extends BaseActorProxy> {

    private final String id;
    private final U initiator;
    private final IMethod<T> method;
    private String sessionId;
    private String password;

    public Request(@NonNull IMethod<T> method, @NonNull U initiator) {
        this(UUID.randomUUID().toString(), initiator, method, null, null);
    }
}
