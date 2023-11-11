package nostr.si4n6r.storage.fs;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import nostr.si4n6r.core.impl.AccountProxy;
import nostr.si4n6r.core.impl.BaseActorProxy;
import nostr.si4n6r.storage.Vault;
import nostr.si4n6r.util.EncryptionUtil;

@Data
@AllArgsConstructor
public abstract class BaseFSVault<T extends BaseActorProxy> implements Vault<T> {

    private final String baseDirectory;
    private final String entityName;

    @Override
    public boolean contains(T entity) {
        return null != this.retrieve(entity);
    }

    protected String getBaseEntityDirectory() {
        return getBaseDirectory() + "/" + entityName;
    }

    protected String getActorBaseDirectory(@NonNull BaseActorProxy proxy) {
        return this.getBaseDirectory() + File.separator + getEntityName() + File.separator + EncryptionUtil.generateCRC32Hash(proxy.getPublicKey());
    }

    protected abstract String getBaseDirectory(@NonNull T entity);
}
