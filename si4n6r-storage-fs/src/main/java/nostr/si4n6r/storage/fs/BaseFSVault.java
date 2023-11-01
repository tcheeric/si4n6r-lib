package nostr.si4n6r.storage.fs;

import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.si4n6r.storage.Vault;
import nostr.si4n6r.storage.model.BaseEntity;

@Data
@AllArgsConstructor
public abstract class BaseFSVault<T extends BaseEntity> implements Vault<T> {

    private final String baseDirectory;
    private final String entityName;

    public boolean contains(String key) {
        return null != this.retrieveNsec(key);
    }

    protected String getBaseEntityDirectory() {
        return getBaseDirectory() + "/" + entityName;
    }
}
