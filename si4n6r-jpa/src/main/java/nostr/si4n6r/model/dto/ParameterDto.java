package nostr.si4n6r.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.si4n6r.model.dto.RequestDto;

/**
 * DTO for {@link nostr.si4n6r.model.Parameter}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterDto extends BaseDto {
    @NotNull
    private String name;
    @NotNull
    private String value;
    @NotNull
    private RequestDto request;
}