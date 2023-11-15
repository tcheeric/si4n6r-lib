package nostr.si4n6r.storage.fs;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.si4n6r.core.impl.Principal;
import nostr.si4n6r.core.impl.SecurityManager;
import nostr.si4n6r.core.impl.AccountProxy;
import nostr.si4n6r.util.PasswordGenerator;
import nostr.si4n6r.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import nostr.si4n6r.core.impl.ApplicationProxy;
import static nostr.si4n6r.core.impl.BaseActorProxy.VAULT_ACTOR_ACCOUNT;
import static nostr.si4n6r.core.impl.BaseActorProxy.VAULT_ACTOR_APPLICATION;
import static nostr.si4n6r.util.EncryptionUtil.getPrivateKeyFile;

import nostr.si4n6r.util.EncryptionUtil;

@Log
@Getter
public class NostrAccountFSVault extends BaseFSVault<AccountProxy> {

    private String password;

    public NostrAccountFSVault() {
        super(Util.getAccountBaseDirectory(), VAULT_ACTOR_ACCOUNT);
        this.password = PasswordGenerator.generate(16);
    }

    @Override
    public boolean store(@NonNull AccountProxy account) {

        var baseDirectory = getBaseDirectory(account);
        var baseDirectoryPath = Path.of(baseDirectory);

        if (!Files.exists(baseDirectoryPath)) {
            try {
                Files.createDirectories(baseDirectoryPath);

                var accountFileLocation = baseDirectory + File.separator + "nsec.bin";
                var path = Path.of(accountFileLocation);

                log.log(Level.INFO, "Store nsec in {0}", path.toString());

                storeNsec(account, path, password);

                linkApplication(account, path);

                return true;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public String retrieve(@NonNull AccountProxy account) {
        String baseDirectory = getBaseDirectory(account);
        Path privateKeyPath = Path.of(baseDirectory, "nsec.bin");

        if (Files.exists(privateKeyPath)) {
            try {
                var securityManager = SecurityManager.getInstance();
                var principal = securityManager.getPrincipal(new PublicKey(account.getPublicKey()));
                var nsec = principal.decryptNsec();
                return nsec.toString();
            } catch (Exception ex) {
                log.log(Level.SEVERE, String.format("Failed to decrypt the nsec for %s", account.getPublicKey()), ex);
                return null;
            }
        }

        return null;
    }

    @Override
    protected String getBaseDirectory(@NonNull AccountProxy account) {
        return getActorBaseDirectory(account);
    }

    private static void createNSecSymbolicLink(AccountProxy account, Path target) throws IOException {
        final var nsecPath = _getAccountApplicationBaseDir(account);
        var link = Paths.get(nsecPath, EncryptionUtil.generateCRC32Hash(account.getPublicKey()) + ".nsec");
        if (Files.exists(link)) {
            Files.delete(link);
        }
        Files.createSymbolicLink(link, target);
    }

    private static void createAppSymbolicLinkUnderAccountApp(AccountProxy account) throws IOException {
        var appFilePath = _createAccountApplicationsBaseDir(account);
        var link = Paths.get(appFilePath, EncryptionUtil.generateCRC32Hash(account.getApplication().getPublicKey()) + ".app");
        if (Files.exists(link)) {
            Files.delete(link);
        }

        var appVault = new NostrApplicationFSVault();
        var target = appVault.getBaseDirectory(account.getApplication()) + File.separator + "metadata.json";
        Files.createSymbolicLink(link, Paths.get(target));
    }

    private void storeApplication(@NonNull ApplicationProxy application) {
        var applicationVault = new NostrApplicationFSVault();
        applicationVault.store(application);
    }

    private static void storeNsec(@NonNull AccountProxy account, @NonNull Path path, @NonNull String password) throws Exception {
        if (!Files.exists(path)) {
            var nsec = _getNsec(account, password);
            Files.write(path, nsec, StandardOpenOption.CREATE);
        }
    }

    private static byte[] _getNsec(AccountProxy account, @NonNull String password) throws Exception {
        var publicKey = EncryptionUtil.generateAndSavePrivateKey(getPrivateKeyFile(new PublicKey(account.getPublicKey())), password) ;
        return Principal.encryptNsec(new PrivateKey(account.getPrivateKey()), publicKey);
    }

    private static String _createAccountApplicationsBaseDir(@NonNull AccountProxy account) throws IOException {
        var appFilePath = _getAccountApplicationBaseDir(account);
        var appPath = Path.of(appFilePath);
        if (Files.notExists(appPath)) {
            Files.createDirectories(appPath);
        }
        return appFilePath;
    }

    private static String _getAccountApplicationBaseDir(@NonNull AccountProxy account) {
        var accountBaseDir = __getAccountBaseDirectory(account);
        return accountBaseDir + File.separator + VAULT_ACTOR_APPLICATION + File.separator + EncryptionUtil.generateCRC32Hash(account.getApplication().getPublicKey());
    }

    private static String __getAccountBaseDirectory(@NonNull AccountProxy account) {
        var accountVault = new NostrAccountFSVault();
        return accountVault.getBaseDirectory(account);
    }

    private void linkApplication(@NonNull AccountProxy accountProxy, @NonNull Path path) throws IOException {

        if(accountProxy.getApplication() == null) {
            throw new IllegalArgumentException("Application is null");
        }

        log.log(Level.INFO, "Store application...");

        storeApplication(accountProxy.getApplication());

        log.log(Level.INFO, "Create App symlink under account app...");

        createAppSymbolicLinkUnderAccountApp(accountProxy);

        log.log(Level.INFO, "Create nsec symlink...");

        createNSecSymbolicLink(accountProxy, path);
    }

}
