package nostr.si4n6r.storage.fs;

import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.si4n6r.storage.model.BaseEntity;
import nostr.si4n6r.storage.Vault;

@Data
@AllArgsConstructor
public abstract class BaseFSVault<T extends BaseEntity> implements Vault<T> {

    private final String baseDirectory;
    private final String entityName;

    public boolean contains(String key) {
        return null != this.retrieve(key);
    }

    protected String getBaseEntityDirectory() {
        return getBaseDirectory() + "/" + entityName;
    }
}
