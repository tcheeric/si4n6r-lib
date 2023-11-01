package nostr.si4n6r.storage.fs;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.si4n6r.core.impl.SecurityManager;
import nostr.si4n6r.storage.model.NostrAccount;
import nostr.si4n6r.storage.model.VaultEntity;
import nostr.si4n6r.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static nostr.si4n6r.storage.Vault.VAULT_ENTITY_ACCOUNT;

@Log
@VaultEntity(name = VAULT_ENTITY_ACCOUNT)
public class NostrAccountFSVault extends BaseFSVault<NostrAccount> {

    private static NostrAccountFSVault instance;

    public NostrAccountFSVault() {
        super(Util.getAccountBaseDirectory(), VAULT_ENTITY_ACCOUNT);
    }

    public static NostrAccountFSVault getInstance() {
        if (instance == null) {
            instance = new NostrAccountFSVault();
        }
        return instance;
    }

    @Override
    public boolean store(@NonNull NostrAccount account) {
        var baseDirectory = getBaseDirectory(account.getPublicKey());
        var baseDirectoryPath = Path.of(baseDirectory);

        if (!Files.exists(baseDirectoryPath)) {
            try {
                Files.createDirectories(baseDirectoryPath);

                var accountFilePath = baseDirectory + File.separator + "nsec.bin";
                var path = Path.of(accountFilePath);

                storeNsec(account, path);

                storeAccountApplication(account, baseDirectory);

                return true;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public String retrieve(@NonNull String publicKey) {
        String baseDirectory = getBaseDirectory(publicKey);
        Path privateKeyPath = Path.of(baseDirectory, "nsec.bin");

        if (Files.exists(privateKeyPath)) {
            var securityManager = SecurityManager.getInstance();
            var principal = securityManager.getPrincipal(new PublicKey(publicKey));
            var nsec = principal.decryptNsec();
            return nsec.toString();
        }

        return null;
    }

    private String getBaseDirectory(@NonNull String publicKey) {
        return getBaseEntityDirectory() + File.separator + publicKey;
    }

    private void storeAccountApplication(NostrAccount account, String baseDirectory) throws IOException {
        if (account.getApplication() != null) {
            var applicationVault = NostrApplicationFSVault.getInstance(getBaseDirectory());
            applicationVault.store(account.getApplication());

            var appFilePath = createApplicationsBaseDir(baseDirectory);

            // store application public key as a file
            var appPath = Path.of(appFilePath, account.getApplication().getPublicKey());
            if (Files.notExists(appPath)) {
                Files.createFile(appPath);
            }
        }
    }

    private static String createApplicationsBaseDir(String baseDirectory) throws IOException {
        var appFilePath = baseDirectory + File.separator + "applications";
        var appPath = Path.of(appFilePath);
        if (Files.notExists(appPath)) {
            Files.createDirectories(appPath);
        }
        return appFilePath;
    }

    private static void storeNsec(NostrAccount account, Path path) throws Exception {
        if (!Files.exists(path)) {
            var securityManager = SecurityManager.getInstance();
            var principal = securityManager.getPrincipal(new PublicKey(account.getPublicKey()));
            var nsec = principal.encryptNsec(new PrivateKey(account.getPrivateKey()));
            Files.write(path, nsec, StandardOpenOption.CREATE);
        }
    }
}
