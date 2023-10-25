import nostr.si4n6r.signer.provider.SignerCommandHandler;
import nostr.ws.handler.command.spi.ICommandHandler;

module si4n6r.signer {

    requires lombok;
    requires java.logging;

    requires nostr.api;
    requires nostr.base;
    requires nostr.client;
    requires nostr.crypto;
    requires nostr.event;
    requires nostr.id;
    requires nostr.util;
    requires nostr.ws.handler;

    requires si4n6r.core;
    requires si4n6r.util;

    exports nostr.si4n6r.signer;

    provides ICommandHandler with SignerCommandHandler;

}