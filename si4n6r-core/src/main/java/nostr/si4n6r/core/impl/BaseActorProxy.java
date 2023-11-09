package nostr.si4n6r.core.impl;

import lombok.Data;

@Data
public abstract class BaseActorProxy {

    public static String VAULT_ACTOR_ACCOUNT = "account";
    public static String VAULT_ACTOR_APPLICATION = "application";

    private Long id;
    private String publicKey;
}
