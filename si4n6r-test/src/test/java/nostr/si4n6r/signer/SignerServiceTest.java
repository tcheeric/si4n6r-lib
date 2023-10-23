package nostr.si4n6r.signer;

import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.id.Identity;
import nostr.si4n6r.core.IMethod;
import nostr.si4n6r.core.impl.Request;
import nostr.si4n6r.core.impl.Session;
import nostr.si4n6r.core.impl.methods.Connect;
import nostr.si4n6r.core.impl.methods.Describe;
import nostr.si4n6r.core.impl.methods.Disconnect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SignerServiceTest {

    private SignerService signerService;

    @BeforeAll
    public void setUp() {
        this.signerService = SignerService.getInstance(Relay.fromString("wss://relay.badgr.space"));
    }

    @Test
    @DisplayName("Signer-Initiated connect")
    public void connect() throws Session.SessionTimeoutException {
        var app = Identity.generateRandomIdentity().getPublicKey();
        this.signerService.connect(app);

        var sessionManager = this.signerService.getSessionManager();

        assertTrue(sessionManager.hasActiveSession(app));
        var session = sessionManager.getSession(app);

        var request = session.getRequests()
                .stream()
                .filter(r -> r.getSessionId().equals(session.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(request);
    }

    @Test
    @DisplayName("Handle app-initiated connect request")
    public void handleConnect() {
        var app = Identity.generateRandomIdentity().getPublicKey();
        var request = new Request(new Connect(app), app);
        this.signerService.handle(request);

        assertEquals("ACK", request.getMethod().getResult());
        assertTrue(this.signerService.getSessionManager().hasActiveSession(app));
    }

    @Test
    @DisplayName("Handle disconnect request")
    public void handleDisconnect() {
        var app = Identity.generateRandomIdentity().getPublicKey();
        var request = new Request(new Connect(app), app);
        this.signerService.handle(request);
        assertFalse(Session.getInstance(app).hasTimedOut());

        request = new Request(new Disconnect(), app);
        var session = this.signerService.getSessionManager().getSession(app);
        request.setSessionId(session.getId());
        this.signerService.handle(request);

        assertEquals("ACK", request.getMethod().getResult());
        assertThrows(RuntimeException.class, () -> {
            this.signerService.getSessionManager().getSession(app);
        });
    }

    @Test
    @DisplayName("Handle describe request")
    public void handleDescribe() {
        var app = Identity.generateRandomIdentity().getPublicKey();
        var request = new Request(new Connect(app), app);
        this.signerService.handle(request);
        assertFalse(Session.getInstance(app).hasTimedOut());

        request = new Request(new Describe(), app);
        var session = this.signerService.getSessionManager().getSession(app);
        request.setSessionId(session.getId());
        this.signerService.handle(request);

        var result = request.getMethod().getResult();
        assertTrue(result instanceof List);
        assertEquals(3, ((List<?>) result).size());
        assertTrue(((List) result).contains(IMethod.Constants.METHOD_CONNECT));
        assertTrue(((List) result).contains(IMethod.Constants.METHOD_DISCONNECT));
        assertTrue(((List) result).contains(IMethod.Constants.METHOD_DESCRIBE));
    }

    @Test
    @DisplayName("Handle describe request without connect")
    public void handleDescribeWithoutConnect() {
        var app = Identity.generateRandomIdentity().getPublicKey();
        var request = new Request(new Describe(), app);

        assertThrows(RuntimeException.class, () -> {
            this.signerService.handle(request);
        });
    }
}
