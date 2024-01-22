package nostr.si4n6r.signer.methods;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.si4n6r.core.Method;
import nostr.si4n6r.core.NIP46Method;
import nostr.si4n6r.core.impl.Parameter;

import static nostr.si4n6r.core.IMethod.Constants.METHOD_DISCONNECT;

@NIP46Method(name = METHOD_DISCONNECT)
public class Disconnect extends Method<String> {

    public Disconnect(@NonNull PublicKey publicKey) {
        super();
        addParam(new Parameter("pubkey", publicKey));
    }
}
