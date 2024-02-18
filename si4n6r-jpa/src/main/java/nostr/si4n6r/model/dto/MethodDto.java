package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for {@link nostr.si4n6r.model.Method}
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodDto extends BaseDto {

    @Getter
    public enum MethodType {
        DESCRIBE("describe", 11L),
        CONNECT("connect", 1L),
        DISCONNECT("disconnect", 12L),
        GET_PUBLIC_KEY("get_public_key", 2L),
        SIGN_EVENT("sign_event", 3L);

        private final String name;
        private final Long id;

        MethodType(@NonNull String name, @NonNull Long id) {
            this.name = name;
            this.id = id;
        }

    }

    @NotNull
    private String name;
    private String description;
}