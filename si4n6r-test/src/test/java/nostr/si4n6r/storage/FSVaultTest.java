package nostr.si4n6r.storage;

import nostr.base.PublicKey;
import nostr.id.Identity;
import nostr.si4n6r.core.impl.Principal;
import nostr.si4n6r.core.impl.SecurityManager;
import nostr.si4n6r.core.impl.AccountProxy;
import nostr.si4n6r.core.impl.ApplicationProxy;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.ServiceLoader;
import lombok.NonNull;

import static org.junit.jupiter.api.Assertions.*;
import static nostr.si4n6r.core.impl.BaseActorProxy.VAULT_ACTOR_ACCOUNT;
import static nostr.si4n6r.core.impl.BaseActorProxy.VAULT_ACTOR_APPLICATION;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FSVaultTest {

    private Vault<AccountProxy> accountVault;
    private Vault<ApplicationProxy> applicationVault;
    private AccountProxy account;
    private ApplicationProxy application;

    @BeforeAll
    public void setUp() {
        // NOTE: Do not change the order these two methods are invoked.
        createApplication();
        createAccount();

        this.accountVault = getAccountVault();
        this.applicationVault = getApplicationVault();
    }

    @Test
    @DisplayName("Store and retrieve an account to the FS vault")
    public void account() {

        final var principalPublicKey = new PublicKey(account.getApplication().getPublicKey());
        final Principal principal = Principal.getInstance(principalPublicKey, "password");
        var flag = SecurityManager.getInstance().addPrincipal(principal);
        assertTrue(flag);

        var stored = accountVault.store(account);
        System.out.println("Account Vault: " + accountVault);

        Assertions.assertTrue(stored);

        stored = accountVault.store(account);
        Assertions.assertFalse(stored);

    }

    @Test
    @DisplayName("Store and retrieve an application to the FS vault")
    public void application() {

        applicationVault.store(application);

        var metadata = applicationVault.retrieve(application);
        var template = application.getTemplate();
        assertTrue(metadata.contains(template.getName()));
        assertTrue(metadata.contains(template.getDescription()));
        assertTrue(metadata.contains(template.getUrl()));
        assertTrue(metadata.contains(template.getIcons().get(0)));
        assertTrue(metadata.contains(template.getIcons().get(1)));
    }

    private void createApplication() {
        var identity = Identity.generateRandomIdentity();
        this.application = new ApplicationProxy(identity.getPublicKey());
        this.application.setId(System.currentTimeMillis());
        var now = System.currentTimeMillis();
        this.application.setName("shibboleth_" + now);
        var template = this.application.getTemplate();
        template.setName("shibboleth_" + now);
        template.setUrl("https://nostr.com");
        template.setDescription("A nip-46 compliant nostr application");
        template.setIcons(List.of("https://nostr.com/favicon.ico", "https://nostr.com/favicon.png"));
    }

    private void createAccount() {
        var identity = Identity.generateRandomIdentity();
        this.account = new AccountProxy();
        account.setPublicKey(identity.getPublicKey().toString());
        account.setPrivateKey(identity.getPrivateKey().toString());
        account.setId(System.currentTimeMillis());
        account.setApplication(this.application);
    }

    private static Vault<AccountProxy> getAccountVault() {
        return getVault(VAULT_ACTOR_ACCOUNT);
    }

    private static Vault<ApplicationProxy> getApplicationVault() {
        return getVault(VAULT_ACTOR_APPLICATION);
    }

    private static Vault getVault(@NonNull String entity) {
        return ServiceLoader
                .load(Vault.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(v -> entity.equals(v.getEntityName()))
                .findFirst()
                .get();
    }

}
