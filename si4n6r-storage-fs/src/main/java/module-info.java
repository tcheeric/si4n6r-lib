import nostr.si4n6r.storage.Vault;
import nostr.si4n6r.storage.fs.NostrAccountFSVault;
import nostr.si4n6r.storage.fs.NostrApplicationFSVault;

module si4n6r.storage.fs {

    requires lombok;
    requires java.logging;

    requires com.fasterxml.jackson.databind;

    requires si4n6r.storage;
    requires si4n6r.core;
    requires si4n6r.util;

    requires nostr.base;

    exports nostr.si4n6r.storage.fs;

    provides Vault with NostrAccountFSVault, NostrApplicationFSVault;
}