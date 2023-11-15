/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.si4n6r.auth;

import nostr.base.PublicKey;
import nostr.id.Identity;
import nostr.si4n6r.core.impl.AccountProxy;
import nostr.si4n6r.core.impl.ApplicationProxy;
import nostr.si4n6r.core.impl.Principal;
import nostr.si4n6r.core.impl.SecurityManager;
import nostr.si4n6r.registration.AccountRegistration;
import nostr.si4n6r.registration.ApplicationRegistration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 *
 * @author eric
 */
public class Si4n6rAuthTest {

    @Test
    @DisplayName("Register a new account")
    public void registerAccount() {

        var applicationIdentity = Identity.generateRandomIdentity();
        final String appPublicKey = applicationIdentity.getPublicKey().toString();

        ApplicationProxy.ApplicationTemplate template = new ApplicationProxy.ApplicationTemplate();
        template.setName("Test Application");
        template.setUrl("https://github.com/tcheeric/demo-nip46-app");
        template.setDescription("This is a test application");
        template.setIcons(Arrays.stream(new String[]{"https://raw.githubusercontent.com/mbarulli/nostr-logo/main/PNG/nostr-icon-purple-256x256.png"}).toList());

        var applicationProxy = new ApplicationProxy(appPublicKey, template);
        applicationProxy.setName("Test Application_" + System.currentTimeMillis());

        var appRegistration = new ApplicationRegistration();
        appRegistration.register(applicationProxy);

        var accountIdentity = Identity.generateRandomIdentity();
        final String accountPrivateKey = accountIdentity.getPrivateKey().toString();
        final String accountPublicKey = accountIdentity.getPublicKey().toString();

        var accountProxy = new AccountProxy();
        accountProxy.setPrivateKey(accountPrivateKey);
        accountProxy.setPublicKey(accountPublicKey);
        accountProxy.setApplication(applicationProxy);

        var securityManager = SecurityManager.getInstance();

        Assertions.assertThrows(RuntimeException.class, () -> {
            securityManager.getPrincipal(new PublicKey(applicationProxy.getPublicKey()));
        }, "Principal not found!");

        var accountRegistration = new AccountRegistration("password");
        accountRegistration.register(accountProxy);

    }

    @Test
    @DisplayName("Add a principal to the security manager")
    public void getPrincipal() {
        var applicationIdentity = Identity.generateRandomIdentity();
        final String appPublicKey = applicationIdentity.getPublicKey().toString();

        ApplicationProxy.ApplicationTemplate template = new ApplicationProxy.ApplicationTemplate();
        template.setName("Test Application");
        template.setUrl("https://github.com/tcheeric/demo-nip46-app");
        template.setDescription("This is a test application");
        template.setIcons(Arrays.stream(new String[]{"https://raw.githubusercontent.com/mbarulli/nostr-logo/main/PNG/nostr-icon-purple-256x256.png"}).toList());

        var applicationProxy = new ApplicationProxy(appPublicKey, template);
        applicationProxy.setName("Test Application_" + System.currentTimeMillis());

        var securityManager = SecurityManager.getInstance();

        Assertions.assertThrows(RuntimeException.class, () -> {
            securityManager.getPrincipal(new PublicKey(applicationProxy.getPublicKey()));
        }, "Principal not found!");

        securityManager.addPrincipal(Principal.getInstance(new PublicKey(applicationProxy.getPublicKey()), "password"));
        var principal = securityManager.getPrincipal(new PublicKey(applicationProxy.getPublicKey()));

        Assertions.assertNotNull(principal);

    }

}
