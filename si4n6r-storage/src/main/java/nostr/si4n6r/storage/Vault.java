package nostr.si4n6r.storage;

import nostr.si4n6r.storage.model.BaseEntity;

public interface Vault<T extends BaseEntity> {

    String VAULT_ENTITY_ACCOUNT = "account";
    String VAULT_ENTITY_APPLICATION = "application";

    boolean store(T entity);

    String retrieve(String key);

    boolean contains(String key);
}
