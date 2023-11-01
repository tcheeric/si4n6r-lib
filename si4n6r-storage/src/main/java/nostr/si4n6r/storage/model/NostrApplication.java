package nostr.si4n6r.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NostrApplication extends BaseEntity {
    private String name;
    private String description;
    private String publicKey;
    private String url;
    private List<String> icons;
}
