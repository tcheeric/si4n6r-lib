package nostr.si4n6r.storage.fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.si4n6r.core.impl.ApplicationProxy;
import nostr.si4n6r.core.impl.BaseActorProxy;
import nostr.si4n6r.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static nostr.si4n6r.core.impl.BaseActorProxy.VAULT_ACTOR_APPLICATION;


@Log
public class NostrApplicationFSVault extends BaseFSVault<ApplicationProxy> {

    public NostrApplicationFSVault() {
        super(Util.getAccountBaseDirectory(), VAULT_ACTOR_APPLICATION);
    }

    public NostrApplicationFSVault(@NonNull String baseDirectory) {
        super(baseDirectory, VAULT_ACTOR_APPLICATION);
    }

    @Override
    public boolean store(ApplicationProxy application) {
        var baseDirectory = getBaseDirectory(application);
        var baseDirectoryPath = Path.of(baseDirectory);

        if (!Files.exists(baseDirectoryPath)) {
            try {
                Files.createDirectories(baseDirectoryPath);

                var metadataPath = baseDirectory + File.separator + "metadata.json";
                var path = Path.of(metadataPath);

                try {
                    var data = new ObjectMapper().writeValueAsBytes(application.getTemplate());
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
    public String retrieve(@NonNull ApplicationProxy application) {
        var baseDirectory = getBaseDirectory(application);
        Path metadataPath = Path.of(baseDirectory, "metadata.json");

        if (Files.exists(metadataPath)) {
            try {
                byte[] appData = Files.readAllBytes(metadataPath);
                return new String(appData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    protected String getBaseDirectory(@NonNull ApplicationProxy application) {
        return getActorBaseDirectory(application);
    }

    @Override
    protected String getActorBaseDirectory(@NonNull BaseActorProxy proxy) {
        if (proxy instanceof ApplicationProxy applicationProxy) {
            return this.getBaseDirectory() + File.separator + getEntityName() + File.separator + applicationProxy.getName();
        }
        throw new IllegalArgumentException("Invalid proxy type");
    }

}
