package nostr.si4n6r.core;

import lombok.NonNull;

import java.util.List;

public interface IMethod<T> {

    String getName();

    List<IParameter> getParams();

    @NonNull IParameter getParameter(@NonNull String name);

    T getResult();

    interface Constants {
        String METHOD_DESCRIBE = "describe";
        String METHOD_GET_PUBLIC_KEY = "get_public_key";
        String METHOD_SIGN_EVENT = "sign_event";
        String METHOD_CONNECT = "connect";
        String METHOD_DISCONNECT = "disconnect";

        String METHOD_DELEGATE = "delegate";
        String METHOD_GET_RELAYS = "get_relays";
        String METHOD_NIP04_ENCRYPT = "nip04_encrypt";
        String METHOD_NIP04_DECRYPT = "nip04_decrypt";
    }
}
