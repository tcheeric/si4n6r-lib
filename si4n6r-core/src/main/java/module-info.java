module si4n6r.core {

    requires lombok;
    requires java.logging;

    requires nostr.base;

    exports nostr.si4n6r.core;
    exports nostr.si4n6r.core.impl.methods;
    exports nostr.si4n6r.core.impl;
}