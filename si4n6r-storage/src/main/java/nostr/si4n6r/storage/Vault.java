package nostr.si4n6r.storage;

import nostr.si4n6r.core.impl.BaseActorProxy;

public interface Vault<T extends BaseActorProxy> {

    boolean store(T entity);
    
    String getEntityName();

    String retrieve(T entity);

    boolean contains(T entity);
}
