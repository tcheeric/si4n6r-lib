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
    private U initiator;
    private final IMethod<T> method;
    private String jwt;
    private final Date timestamp;

    public Request(@NonNull IMethod<T> method) {
        this(method, null);
    }

    public Request(@NonNull IMethod<T> method, @NonNull String jwt) {
        this(UUID.randomUUID().toString(), null, method, jwt, new Date());
    }
}
