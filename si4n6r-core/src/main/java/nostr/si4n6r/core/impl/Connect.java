package nostr.si4n6r.core.impl;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.si4n6r.core.Method;
import nostr.si4n6r.core.NIP46Method;
import nostr.si4n6r.core.Parameter;

import static nostr.si4n6r.core.IMethod.Constants.METHOD_CONNECT;

@NIP46Method(name = METHOD_CONNECT)
public class Connect extends Method<String> {

    public Connect(@NonNull PublicKey publicKey) {
        super();
        addParam(new Parameter("pubkey", publicKey));
        // TODO - Add an additional parameter that determines the session timeout. When missing, use the default value.
    }

}
