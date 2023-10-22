package nostr.si4n6r.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.si4n6r.core.impl.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Data
@Log
@AllArgsConstructor
public abstract class Method<U> implements IMethod<U> {

    private final List<IParameter> params;
    private U result;

    public Method() {
        this(new ArrayList<>(), null);
    }

    public Method(@NonNull U result) {
        this(new ArrayList<>(), result);
    }

    private static String getNIP46MethodName(Class<?> clazz) {
        var nip46Method = clazz.getAnnotation(NIP46Method.class);
        if (nip46Method != null) {
            return nip46Method.name();
        }
        return null;
    }

    @Override
    @ToString.Include
    public String getName() {
        return getNIP46MethodName(getClass());
    }

    @Override
    public IParameter getParameter(@NonNull String name) {
        log.log(Level.FINER, "Retrieving parameter {0}", name);
        return this.params.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No parameter found with name " + name));
    }

    protected boolean addParam(@NonNull Parameter param) {
        log.log(Level.FINER, "Adding parameter {0}", param);
        if (params.contains(param)) {
            return false;
        }
        this.params.add(param);
        return true;
    }
}
