package nostr.si4n6r.core.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.si4n6r.core.IMethod;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Request<T, U extends BaseActorProxy> {

    private final String id;
    private final U initiator;
    private final IMethod<T> method;
    private String sessionId;
    private final Date timestamp;

    public Request(@NonNull IMethod<T> method, @NonNull U initiator) {
        this(UUID.randomUUID().toString(), initiator, method, null, new Date());
    }

    public Request(@NonNull IMethod<T> method, @NonNull U initiator, @NonNull String sessionId) {
        this(UUID.randomUUID().toString(), initiator, method, sessionId, new Date());
    }
}
