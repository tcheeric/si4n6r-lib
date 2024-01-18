package nostr.si4n6r.core.impl;

import nostr.base.PublicKey;
import nostr.id.Identity;
import nostr.si4n6r.signer.methods.Connect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SessionManagerTest {

    private SessionManager sessionManager;
    private PublicKey publicKey;

    @BeforeAll
    public void setUp() {
        this.sessionManager = SessionManager.getInstance();
        this.publicKey = new PublicKey("9cb64796ed2c5f18846082cae60c3a18d7a506702cdff0276f86a2ea68a94123");
        this.sessionManager.createSession(Identity.generateRandomIdentity().getPublicKey(), publicKey, 20*60, "password");
    }

    @Test
    @DisplayName("Invalidate a session")
    public void invalidate() {
        assertFalse(sessionManager.hasExpired(publicKey));
        sessionManager.deactivateSession(publicKey);

        assertFalse(sessionManager.sessionIsActive(publicKey));
        assertTrue(sessionManager.sessionIsInactive(publicKey));
    }

    @Test
    @DisplayName("Add a request to the session")
    public void addRequest() {
        var appProxy = new ApplicationProxy(publicKey);
        appProxy.setId(System.currentTimeMillis());
        appProxy.setName("addRequest");

        var session = this.sessionManager.getSession(this.publicKey);
        var request = new Request<>(new Connect(this.publicKey), appProxy, session.getId());
        this.sessionManager.addRequest(request, this.publicKey);

        assertTrue(session.getRequests().contains(request));
        assertEquals(session.getId(), request.getSessionId());
    }
}
