module si4n6r.util {

    requires lombok;

    requires si4n6r.core;

    requires nostr.api;
    requires nostr.base;
    requires nostr.crypto;
    requires nostr.util;
    requires nostr.event;
    requires nostr.id;

    exports nostr.si4n6r.util;
}