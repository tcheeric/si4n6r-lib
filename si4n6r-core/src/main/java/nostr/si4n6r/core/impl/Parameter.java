package nostr.si4n6r.core.impl;

import lombok.Data;
import lombok.NonNull;
import nostr.si4n6r.core.IParameter;

@Data
public class Parameter implements IParameter {

    private final String name;
    private final Object value;

    public Parameter(@NonNull String name, @NonNull Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return
     */
    @Override
    public Object get() {
        return getValue();
    }
}
