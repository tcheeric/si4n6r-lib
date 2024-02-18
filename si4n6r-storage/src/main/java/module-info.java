import nostr.si4n6r.storage.Vault;

module si4n6r.storage {
    requires lombok;

    requires nostr.base;

    requires com.fasterxml.jackson.databind;

    exports nostr.si4n6r.storage;
    exports nostr.si4n6r.storage.common;

    uses Vault;
}