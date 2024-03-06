package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for {@link nostr.si4n6r.model.Parameter}
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterDto extends BaseDto {

    public static final String PARAM_EVENT = "event";
    public static final String PARAM_SIGNATURE = "signature";
    public static final String PARAM_PUBLIC_KEY = "publickey";
    public static final String PARAM_PLAINTEXT = "plaintext";
    public static final String PARAM_CIPHERTEXT = "ciphertext";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_RELAYS = "relays";

    @NotNull
    private String name;
    @NotNull
    private String value;
    @NotNull
    private RequestDto request;
}