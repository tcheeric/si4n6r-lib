package nostr.si4n6r.core.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.si4n6r.util.EncryptionUtil;
import nostr.si4n6r.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

@Data
@EqualsAndHashCode
@Log
public class Principal {

    private static Principal instance;

    private final PublicKey npub;

    @EqualsAndHashCode.Exclude
    private final String nsecBinPath;

    @EqualsAndHashCode.Exclude
    private String password;
    @EqualsAndHashCode.Exclude
    private java.security.PublicKey publicKey;

    private Principal(@NonNull PublicKey npub, @NonNull String password) {
        this.npub = npub;
        this.password = password;
        this.nsecBinPath = Util.getAccountBaseDirectory() + File.separator + "account" + File.separator + npub + File.separator + "nsec.bin";
    }

    public static Principal getInstance(@NonNull PublicKey npub, @NonNull String password) {
        if (instance == null || !SecurityManager.getInstance().hasPrincipal(npub, password)) {
            instance = new Principal(npub, password);
        }
        return instance;
    }

    public static Principal getInstance(@NonNull AccountProxy account, @NonNull String password) {
        return getInstance(new PublicKey(account.getPublicKey()), password);
    }

    public nostr.base.PrivateKey decryptNsec() throws IOException, Exception {
        String privateKeyFile = EncryptionUtil.getPrivateKeyFile(this.npub);

        // Print the contents of the private key file
        byte[] privateKeyFileBytes = Files.readAllBytes(Path.of(privateKeyFile));
        log.log(Level.INFO, "privateKeyFileBytes: {0}", new String(privateKeyFileBytes));

        var privateKey = EncryptionUtil.loadPrivateKeyFromPEM(privateKeyFile, this.password);
        byte[] nsecRawData = Files.readAllBytes(Path.of(this.nsecBinPath));

        log.log(Level.INFO, "nsecRawData length: {0}", nsecRawData.length);

        var nsec = EncryptionUtil.decryptWithPrivateKey(privateKey, nsecRawData);
        return new nostr.base.PrivateKey(nsec);
    }

    public static byte[] encryptNsec(@NonNull PrivateKey nsec, java.security.PublicKey publicKey) throws Exception {
        return EncryptionUtil.encryptWithPublicKey(publicKey, nsec.getRawData());
    }
}
