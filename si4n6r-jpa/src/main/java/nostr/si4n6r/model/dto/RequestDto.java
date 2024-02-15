package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link nostr.si4n6r.model.Request}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto extends BaseDto {
    private Long id;
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