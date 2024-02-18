package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link nostr.si4n6r.model.Request}
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto extends BaseDto {

    @NotNull
    private String initiator;
    @NotNull
    private String token;
    @NotNull
    private LocalDateTime createdAt = LocalDateTime.now();
    @NotNull
    private String requestUuid = UUID.randomUUID().toString();
    @NotNull
    private SessionDto session;
    @NotNull
    private MethodDto method;
}