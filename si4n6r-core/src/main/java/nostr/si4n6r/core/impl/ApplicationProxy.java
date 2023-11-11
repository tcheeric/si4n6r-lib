package nostr.si4n6r.core.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import lombok.NonNull;
import nostr.base.PublicKey;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationProxy extends BaseActorProxy {

    private String name;
    private String description;
    private String url;
    private List<String> icons;

    public ApplicationProxy(@NonNull String publicKey) {
        this.setPublicKey(publicKey);
    }

    public ApplicationProxy(@NonNull PublicKey publicKey) {
        this(publicKey.toString());
    }
}
