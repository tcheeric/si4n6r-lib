package nostr.si4n6r.core.impl;

import nostr.base.PublicKey;
import nostr.id.Identity;
import nostr.si4n6r.signer.methods.Connect;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SessionManagerTest {

    private SessionManager sessionManager;
    private PublicKey publicKey;

    @BeforeAll
    public void setUp() throws SecurityManager.SecurityManagerException {
        this.sessionManager = SessionManager.getInstance();
        this.publicKey = new PublicKey("9cb64796ed2c5f18846082cae60c3a18d7a506702cdff0276f86a2ea68a94123");
        var securityManager = SecurityManager.getInstance();
        securityManager.addPrincipal(Principal.getInstance(publicKey, "password"));
        sessionManager.addSession(Session.getInstance(publicKey));
    }

    @Test
    @DisplayName("Invalidate a session")
    public void invalidate() {
        assertFalse(sessionManager.hasTimedOut(publicKey));

        sessionManager.invalidate(publicKey);

        assertFalse(sessionManager.hasActiveSession(publicKey));
        assertTrue(sessionManager.hasTimedOut(publicKey));
    }

    @Test
    @DisplayName("Add a request to the session")
    public void addRequest() throws SecurityManager.SecurityManagerException {
        Identity identity = Identity.generateRandomIdentity();
        SecurityManager.getInstance().addPrincipal(Principal.getInstance(identity.getPublicKey(), "password"));
        this.sessionManager.addSession(identity.getPublicKey());

        var appProxy = new ApplicationProxy(publicKey);
        appProxy.setId(System.currentTimeMillis());
        appProxy.setName("addRequest");
        
        var request = new Request<>(new Connect(identity.getPublicKey()), appProxy);
        this.sessionManager.addRequest(request, identity.getPublicKey());

        var session = this.sessionManager.getSession(identity.getPublicKey());

        assertTrue(session.getRequests().contains(request));
        Assertions.assertEquals(session.getId(), request.getSessionId());
    }
}
