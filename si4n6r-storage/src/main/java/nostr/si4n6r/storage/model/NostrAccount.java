package nostr.si4n6r.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NostrAccount extends BaseEntity {
    private String publicKey;
    private String privateKey;
    private NostrApplication application;
}
