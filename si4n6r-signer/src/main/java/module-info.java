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
    
    requires com.fasterxml.jackson.databind;

    exports nostr.si4n6r.signer;
    exports nostr.si4n6r.signer.methods;

    provides ICommandHandler with SignerCommandHandler;

}