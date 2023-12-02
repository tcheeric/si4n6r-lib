module si4n6r.core {

    requires lombok;
    requires java.logging;

    requires nostr.base;

    requires org.bouncycastle.provider;
    requires si4n6r.util;

    requires com.fasterxml.jackson.databind;

    exports nostr.si4n6r.core;
    exports nostr.si4n6r.core.impl;
}