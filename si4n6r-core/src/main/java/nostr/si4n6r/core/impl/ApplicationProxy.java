package nostr.si4n6r.core.impl;

import lombok.*;
import nostr.base.PublicKey;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ApplicationProxy extends BaseActorProxy {

    private final ApplicationTemplate template;
    private String name;

    public ApplicationProxy(@NonNull String publicKey) {
        this(publicKey, new ApplicationTemplate());
    }

    public ApplicationProxy(@NonNull PublicKey publicKey) {
        this(publicKey, new ApplicationTemplate());
    }

    public ApplicationProxy(@NonNull String publicKey, @NonNull ApplicationTemplate template) {
        this.setPublicKey(publicKey);
        this.template = template;
    }

    public ApplicationProxy(@NonNull PublicKey publicKey, @NonNull ApplicationTemplate template) {
        this(publicKey.toString(), template);
    }

    @Data
    @NoArgsConstructor
    public static class ApplicationTemplate {

        private String name;
        private String description;
        private String url;
        private List<String> icons;
    }
}
