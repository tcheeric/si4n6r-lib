package nostr.si4n6r.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class Response {

    private String id;
    private String method;
    private Object result;
    private String error;
    private String sessionId;

    public Response(@NonNull String id, @NonNull String method, @NonNull Object result) {
        this(id, method, result, null, null);
    }
}
