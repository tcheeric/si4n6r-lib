package nostr.si4n6r.storage;

import nostr.si4n6r.storage.common.BaseActorProxy;

public interface Vault<T extends BaseActorProxy> {

    boolean store(T entity);
    
    String getEntityName();

    String retrieve(T entity, String password);

    boolean contains(T entity, String password);
}
