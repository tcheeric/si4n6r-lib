module si4n6r.util {

    requires lombok;
    requires java.logging;

    requires nostr.base;
    requires nostr.event;

    requires com.auth0.jwt;

    exports nostr.si4n6r.util;
}