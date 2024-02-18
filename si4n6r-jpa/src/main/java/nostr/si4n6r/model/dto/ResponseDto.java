package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.si4n6r.model.dto.MethodDto;

import java.time.LocalDateTime;

/**
 * DTO for {@link nostr.si4n6r.model.Response}
 */
@EqualsAndHashCode(callSuper = true)
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
    private String responseUuid;
    @NotNull
    private String result;
    @NotNull
    private LocalDateTime createdAt = LocalDateTime.now();
    @NotNull
    private MethodDto method;
    @NotNull
    private SessionDto session;
}