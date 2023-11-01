package nostr.si4n6r.signer.methods;

import lombok.NonNull;
import nostr.base.IEvent;
import nostr.si4n6r.core.Method;
import nostr.si4n6r.core.NIP46Method;
import nostr.si4n6r.core.impl.Parameter;

import static nostr.si4n6r.core.IMethod.Constants.METHOD_SIGN_EVENT;

@NIP46Method(name = METHOD_SIGN_EVENT)
public class SignEvent extends Method<IEvent> {

    public SignEvent(@NonNull IEvent event) {
        super();
        addParam(new Parameter("event", event));
    }
}
