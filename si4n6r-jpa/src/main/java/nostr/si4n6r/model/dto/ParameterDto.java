package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.si4n6r.model.dto.RequestDto;

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

    @NotNull
    private String name;
    @NotNull
    private String value;
    @NotNull
    private RequestDto request;
}