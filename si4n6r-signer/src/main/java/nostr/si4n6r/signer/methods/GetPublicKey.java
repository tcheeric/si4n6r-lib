package nostr.si4n6r.signer.methods;

import nostr.base.PublicKey;
import nostr.si4n6r.core.Method;
import nostr.si4n6r.core.NIP46Method;

import static nostr.si4n6r.core.IMethod.Constants.METHOD_GET_PUBLIC_KEY;

@NIP46Method(name = METHOD_GET_PUBLIC_KEY)
public class GetPublicKey extends Method<PublicKey> {

    public GetPublicKey() {
        super();
    }
}
