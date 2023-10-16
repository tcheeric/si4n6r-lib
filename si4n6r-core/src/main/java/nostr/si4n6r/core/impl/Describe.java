package nostr.si4n6r.core.impl;

import nostr.si4n6r.core.Method;
import nostr.si4n6r.core.NIP46Method;

import java.util.List;

import static nostr.si4n6r.core.IMethod.Constants.METHOD_DESCRIBE;

@NIP46Method(name = METHOD_DESCRIBE)
public class Describe extends Method<List<String>> {

    public Describe() {
        super();
    }
}
