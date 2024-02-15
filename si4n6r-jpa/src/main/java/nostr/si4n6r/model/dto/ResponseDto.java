package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.si4n6r.model.Session;
import nostr.si4n6r.model.dto.MethodDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for {@link nostr.si4n6r.model.Response}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto extends BaseDto {

    public static final String RESULT_ACK = "ACK";

    public ResponseDto(@NotNull RequestDto request) {
        this.method = request.getMethod();
        this.session = request.getSession();
        this.responseUuid = request.getRequestUuid();
    }

    @NotNull
    private String responseUuid = UUID.randomUUID().toString();
    @NotNull
    private String result;
    @NotNull
    private LocalDateTime createdAt = LocalDateTime.now();
    @NotNull
    private MethodDto method;
    @NotNull
    private SessionDto session;
}