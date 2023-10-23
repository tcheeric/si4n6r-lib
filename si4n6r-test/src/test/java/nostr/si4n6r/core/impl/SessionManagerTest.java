package nostr.si4n6r.core.impl;

import nostr.base.PublicKey;
import nostr.id.Identity;
import nostr.si4n6r.core.impl.methods.Connect;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SessionManagerTest {

    private SessionManager sessionManager;
    private PublicKey publicKey;

    @BeforeAll
    public void setUp() {
        this.sessionManager = SessionManager.getInstance();
        this.publicKey = new PublicKey("9cb64796ed2c5f18846082cae60c3a18d7a506702cdff0276f86a2ea68a94123");
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
    public void addRequest() throws Session.SessionTimeoutException {
        Identity identity = Identity.generateRandomIdentity();
        this.sessionManager.addSession(identity.getPublicKey());

        var request = new Request(new Connect(identity.getPublicKey()), publicKey);
        this.sessionManager.addRequest(request, identity.getPublicKey());

        var session = this.sessionManager.getSession(identity.getPublicKey());

        assertTrue(session.getRequests().contains(request));
        Assertions.assertEquals(session.getId(), request.getSessionId());
    }

    @Test
    @DisplayName("Add a request to an invalid session")
    public void addRequestFails() {
        this.sessionManager.invalidate(publicKey);

        var request = new Request(new Connect(publicKey), publicKey);
        assertThrows(Session.SessionTimeoutException.class, () -> {
            this.sessionManager.addRequest(request, publicKey);
        });
    }

}
