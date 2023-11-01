package nostr.si4n6r.storage.fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.si4n6r.storage.model.NostrApplication;
import nostr.si4n6r.storage.model.VaultEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static nostr.si4n6r.storage.Vault.VAULT_ENTITY_APPLICATION;

@Log
@VaultEntity(name = VAULT_ENTITY_APPLICATION)
public class NostrApplicationFSVault extends BaseFSVault<NostrApplication> {

    private static NostrApplicationFSVault instance;

    public NostrApplicationFSVault() {
        super(System.getProperty("user.home"), VAULT_ENTITY_APPLICATION);
    }

    private NostrApplicationFSVault(@NonNull String baseDirectory) {
        super(baseDirectory, VAULT_ENTITY_APPLICATION);
    }

    public static NostrApplicationFSVault getInstance(@NonNull String baseDirectory) {
        if (instance == null) {
            instance = new NostrApplicationFSVault(baseDirectory);
        }
        return instance;
    }

    @Override
    public boolean store(NostrApplication application) {
        var baseDirectory = getBaseDirectory(application.getPublicKey());
        var baseDirectoryPath = Path.of(baseDirectory);

        if (!Files.exists(baseDirectoryPath)) {
            try {
                Files.createDirectories(baseDirectoryPath);

                var metadataPath = baseDirectory + File.separator +  "metadata.json";
                var path = Path.of(metadataPath);

                try {
                    var data = new ObjectMapper().writeValueAsBytes(application);
                    Files.write(path, data, StandardOpenOption.CREATE);
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public String retrieve(@NonNull String publicKey) {
        var baseDirectory = getBaseDirectory(publicKey);
        Path privateKeyPath = Path.of(baseDirectory, "metadata.json");

        if (Files.exists(privateKeyPath)) {
            try {
                byte[] appData = Files.readAllBytes(privateKeyPath);
                return new String(appData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private String getBaseDirectory(@NonNull String publicKey) {
        return getBaseEntityDirectory() + File.separator + publicKey;
    }
}
