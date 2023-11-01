module si4n6r.storage {
    requires lombok;

    exports nostr.si4n6r.storage;
    exports nostr.si4n6r.storage.model;

    uses nostr.si4n6r.storage.Vault;
}