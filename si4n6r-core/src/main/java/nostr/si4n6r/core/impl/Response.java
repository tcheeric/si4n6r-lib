package nostr.si4n6r.core.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class Response {

    private String id;
    private String method;
    private Object result;
    private String error;
    private Date timestamp;

    public Response(@NonNull String id, @NonNull String method, @NonNull Object result) {
        this(id, method, result, null, new Date());
    }
}
