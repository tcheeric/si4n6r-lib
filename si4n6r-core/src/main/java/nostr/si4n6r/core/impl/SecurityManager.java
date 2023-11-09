package nostr.si4n6r.core.impl;

import lombok.Data;
import lombok.NonNull;
import nostr.base.PublicKey;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.extern.java.Log;

@Data
@Log
// TODO - Add a validate() method to check that the given password can indeed be used to decrypt the nsec.
public class SecurityManager {

    private static SecurityManager instance;
    private final List<Principal> principals;

    private SecurityManager() {
        this.principals = new ArrayList<>();
    }

    public static SecurityManager getInstance() {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }

    public boolean hasPrincipal(@NonNull PublicKey publicKey) {
        return principals.stream()
                .anyMatch(principal -> principal.getNpub().equals(publicKey));
    }

    public boolean hasPrincipal(@NonNull PublicKey publicKey, @NonNull String password) {
        return principals.stream()
                .anyMatch(principal -> principal.getNpub().equals(publicKey) && principal.getPassword().equals(password));
    }

    public boolean addPrincipal(@NonNull Principal principal) {
        log.log(Level.INFO, "Registering {0}...", principal.getNpub());
        if (principals.contains(principal)) {
            return false;
        }

        principals.add(principal);
        return true;
    }

    public void removePrincipal(@NonNull PublicKey publicKey) {
        this.principals.removeIf(principal -> principal.getNpub().equals(publicKey));
    }

    public Principal getPrincipal(@NonNull PublicKey publicKey) {
        return principals.stream()
                .filter(principal -> principal.getNpub().equals(publicKey))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Principal not found!"));
    }

    public void setPassword(@NonNull PublicKey publicKey, @NonNull String password) {
        if (hasPrincipal(publicKey)) {
            getPrincipal(publicKey).setPassword(password);
        } else {
            this.addPrincipal(Principal.getInstance(publicKey, password));
        }
    }

    public static class SecurityManagerException extends Exception {

        public SecurityManagerException(String message) {
            super(message);
        }

        public SecurityManagerException(PublicKey publicKey) {
            super("The public key " + publicKey + " is not registered!");
        }
    }
}
