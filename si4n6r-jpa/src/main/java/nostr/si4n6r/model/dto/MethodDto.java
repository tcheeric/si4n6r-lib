package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link nostr.si4n6r.model.Method}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodDto extends BaseDto {

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

        public String getName() {
            return name;
        }

        public Long getId() {
            return id;
        }
    }

    @NotNull
    private String name;
    private String description;
}