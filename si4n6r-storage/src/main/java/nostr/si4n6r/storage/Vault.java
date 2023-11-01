package nostr.si4n6r.storage;

import nostr.si4n6r.storage.model.BaseEntity;

public interface Vault<T extends BaseEntity> {

    String VAULT_ENTITY_ACCOUNT = "account";
    String VAULT_ENTITY_APPLICATION = "application";

    boolean storeNsec(T entity);

    String retrieveNsec(String key);

    boolean contains(String key);
}
