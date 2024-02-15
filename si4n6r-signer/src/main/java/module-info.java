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

    requires si4n6r.rest;
    requires si4n6r.util;
    requires si4n6r.jpa;

    requires spring.web;
    
    requires com.fasterxml.jackson.databind;
    requires com.auth0.jwt;

    exports nostr.si4n6r.signer;

    provides ICommandHandler with SignerCommandHandler;

}