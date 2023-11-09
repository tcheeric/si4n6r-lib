module si4n6r.storage {
    requires lombok;

    requires si4n6r.core;
    
    exports nostr.si4n6r.storage;

    uses nostr.si4n6r.storage.Vault;
}