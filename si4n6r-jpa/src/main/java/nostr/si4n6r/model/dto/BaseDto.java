package nostr.si4n6r.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
public class BaseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @ToString.Include
    private Long id;

    @ToString.Exclude
    private Map<String, Object> _links;

    public Long getIdFromLinks() {
        if (_links != null && _links.containsKey("self")) {
            var selfLink = _links.get("self");
            var hrefLink = (Map<String, Object>) selfLink;
            if (hrefLink.containsKey("href")) {
                var href = hrefLink.get("href").toString();
                String[] parts = href.split("/");
                return Long.parseLong(parts[parts.length - 1]);
            }
        }
        return null;
    }

}
