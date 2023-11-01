package nostr.si4n6r.storage;

import nostr.base.PublicKey;
import nostr.id.Identity;
import nostr.si4n6r.core.impl.Principal;
import nostr.si4n6r.core.impl.SecurityManager;
import nostr.si4n6r.storage.fs.NostrAccountFSVault;
import nostr.si4n6r.storage.fs.NostrApplicationFSVault;
import nostr.si4n6r.storage.model.NostrAccount;
import nostr.si4n6r.storage.model.NostrApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FSVaultTest {

    private Vault vault;
    private NostrAccount account;
    private NostrApplication application;

    @BeforeAll
    public void setUp() {
        // NOTE: Do not change the order these two methods are invoked.
        initApplication();
        initAccount();
    }

    @Test
    @DisplayName("Store and retrieve an account to the FS vault")
    public void account() {
        vault = NostrAccountFSVault.getInstance();

        SecurityManager.getInstance().addPrincipal(Principal.getInstance(new PublicKey(account.getPublicKey()), "password"));
        var stored = vault.store(account);
        assertTrue(stored);

        stored = vault.store(account);
        assertFalse(stored);

        var privateKey = vault.retrieve(account.getPublicKey());
        assertEquals(account.getPrivateKey(), privateKey);
    }

    @Test
    @DisplayName("Store and retrieve an application to the FS vault")
    public void application() {
        vault = NostrApplicationFSVault.getInstance(System.getProperty("user.home"));

        initApplication();

        var stored = vault.store(application);
        assertTrue(stored);

        stored = vault.store(application);
        assertFalse(stored);

        var metadata = vault.retrieve(application.getPublicKey());
        assertTrue(metadata.contains(application.getPublicKey()));
        assertTrue(metadata.contains(application.getName()));
        assertTrue(metadata.contains(application.getDescription()));
        assertTrue(metadata.contains(application.getPublicKey()));
        assertTrue(metadata.contains(application.getUrl()));
        assertTrue(metadata.contains(application.getIcons().get(0)));
        assertTrue(metadata.contains(application.getIcons().get(1)));
    }

    private void initApplication() {
        var identity = Identity.generateRandomIdentity();
        this.application = new NostrApplication();
        application.setPublicKey(identity.getPublicKey().toString());
        application.setId(System.currentTimeMillis());
        application.setName("shibboleth");
        application.setUrl("https://nostr.com");
        application.setDescription("A nip-46 compliant nostr application");
        application.setIcons(List.of("https://nostr.com/favicon.ico", "https://nostr.com/favicon.png"));
    }

    private void initAccount() {
        var identity = Identity.generateRandomIdentity();
        this.account = new NostrAccount();
        account.setPublicKey(identity.getPublicKey().toString());
        account.setPrivateKey(identity.getPrivateKey().toString());
        account.setId(System.currentTimeMillis());
        account.setApplication(this.application);
    }
}
