package nostr.si4n6r.storage.fs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.si4n6r.core.impl.BaseActorProxy;
import nostr.si4n6r.storage.Vault;
import nostr.si4n6r.util.EncryptionUtil;

import java.io.File;

@Data
@AllArgsConstructor
public abstract class BaseFSVault<T extends BaseActorProxy> implements Vault<T> {

    private final String baseDirectory;
    private final String entityName;

    public BaseFSVault() {
        this.baseDirectory = null;
        this.entityName = null;
    }

    @Override
    public boolean contains(T entity, String password) {
        return null != this.retrieve(entity, password);
    }

    protected String getBaseEntityDirectory() {
        return getBaseDirectory() + "/" + entityName;
    }

    protected String getActorBaseDirectory(@NonNull BaseActorProxy proxy) {
        return this.getBaseDirectory() + File.separator + getEntityName() + File.separator + EncryptionUtil.generateCRC32Hash(proxy.getPublicKey());
    }

    protected abstract String getBaseDirectory(@NonNull T entity);
}
