package nostr.si4n6r.storage.fs;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.si4n6r.core.impl.SecurityManager;
import nostr.si4n6r.core.impl.AccountProxy;
import nostr.si4n6r.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import nostr.si4n6r.core.impl.ApplicationProxy;
import static nostr.si4n6r.core.impl.BaseActorProxy.VAULT_ACTOR_ACCOUNT;
import static nostr.si4n6r.core.impl.BaseActorProxy.VAULT_ACTOR_APPLICATION;
import nostr.si4n6r.util.EncryptionUtil;

@Log
public class NostrAccountFSVault extends BaseFSVault<AccountProxy> {

    public NostrAccountFSVault() {
        super(Util.getAccountBaseDirectory(), VAULT_ACTOR_ACCOUNT);
    }

    public NostrAccountFSVault(@NonNull String baseDirectory) {
        super(baseDirectory, VAULT_ACTOR_APPLICATION);
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

                storeNsec(account, path);

                storeApplication(account.getApplication());

                createAppSymbolicLinkUnderAccountApp(account);

                createNSecSymbolicLink(account, path);

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
            var securityManager = SecurityManager.getInstance();
            var principal = securityManager.getPrincipal(new PublicKey(account.getPublicKey()));
            var nsec = principal.decryptNsec();
            return nsec.toString();
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

    private static void storeNsec(AccountProxy account, Path path) throws Exception {
        if (!Files.exists(path)) {
            byte[] nsec = _getNsec(account);
            Files.write(path, nsec, StandardOpenOption.CREATE);
        }
    }

    private static byte[] _getNsec(AccountProxy account) throws Exception {
        var securityManager = SecurityManager.getInstance();
        var principal = securityManager.getPrincipal(new PublicKey(account.getApplication().getPublicKey()));
        var nsec = principal.encryptNsec(new PrivateKey(account.getPrivateKey()));
        return nsec;
    }

    private static String _createAccountApplicationsBaseDir(@NonNull AccountProxy account) throws IOException {
        String appFilePath = _getAccountApplicationBaseDir(account);
        var appPath = Path.of(appFilePath);
        if (Files.notExists(appPath)) {
            Files.createDirectories(appPath);
        }
        return appFilePath;
    }

    private static String _getAccountApplicationBaseDir(@NonNull AccountProxy account) {
        var accountBaseDir = __getAccountBaseDirectory(account);
        var appFilePath = accountBaseDir + File.separator + VAULT_ACTOR_APPLICATION + File.separator + EncryptionUtil.generateCRC32Hash(account.getApplication().getPublicKey());
        return appFilePath;
    }

    private static String __getAccountBaseDirectory(@NonNull AccountProxy account) {
        var accountVault = new NostrAccountFSVault();
        var baseDirectory = accountVault.getBaseDirectory(account);
        return baseDirectory;
    }
}
