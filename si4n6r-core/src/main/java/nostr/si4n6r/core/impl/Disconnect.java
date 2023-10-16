package nostr.si4n6r.core.impl;

import nostr.si4n6r.core.Method;
import nostr.si4n6r.core.NIP46Method;

import static nostr.si4n6r.core.IMethod.Constants.METHOD_DISCONNECT;

@NIP46Method(name = METHOD_DISCONNECT)
public class Disconnect extends Method<String> {

    public Disconnect() {
        super();
    }
}
