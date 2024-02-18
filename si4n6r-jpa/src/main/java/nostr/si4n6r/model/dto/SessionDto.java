package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link nostr.si4n6r.model.Session}
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDto extends BaseDto {
    @NotNull
    private String sessionId = UUID.randomUUID().toString();
    @NotNull
    private String status;
    @NotNull
    private String account;
    @NotNull
    private String app;
    @NotNull
    private LocalDateTime createdAt = LocalDateTime.now();
    @NotNull
    private String token;
}