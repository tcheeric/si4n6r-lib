package nostr.si4n6r.signer;

import nostr.base.Relay;
import nostr.id.Identity;
import nostr.si4n6r.core.IMethod;
import nostr.si4n6r.core.impl.*;
import nostr.si4n6r.core.impl.SecurityManager;
import nostr.si4n6r.signer.methods.Connect;
import nostr.si4n6r.signer.methods.Describe;
import nostr.si4n6r.signer.methods.Disconnect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

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
    public void connect() throws Session.SessionTimeoutException, SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();
        SecurityManager.getInstance().addPrincipal(Principal.getInstance(app, "password"));
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
    public void handleConnect() throws SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();
        SecurityManager.getInstance().addPrincipal(Principal.getInstance(app, "password"));
        var request = new Request(new Connect(app), app);
        this.signerService.handle(request);

        assertEquals("ACK", request.getMethod().getResult());
        assertTrue(this.signerService.getSessionManager().hasActiveSession(app));
    }

    @Test
    @DisplayName("Handle app-initiated connect request without passwrd being provided")
    public void handleConnectWithoutPassword() throws SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();
        var request = new Request(new Connect(app), app);

        assertThrows(SecurityManager.SecurityManagerException.class, () -> {
            this.signerService.handle(request);
        });
    }

    @Test
    @DisplayName("Handle disconnect request")
    public void handleDisconnect() throws SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();

        var securityManager = SecurityManager.getInstance();
        securityManager.addPrincipal(Principal.getInstance(app, "password"));

        var request = new Request(new Connect(app), app);
        this.signerService.handle(request);
        assertFalse(Session.getInstance(app).hasTimedOut());

        request = new Request(new Disconnect(), app);
        var session = this.signerService.getSessionManager().getSession(app);
        request.setSessionId(session.getId());
        this.signerService.handle(request);

        assertEquals("ACK", request.getMethod().getResult());
        assertThrows(SecurityManager.SecurityManagerException.class, () -> {
            Session.getInstance(app);
        });
    }

    @Test
    @DisplayName("Handle describe request")
    public void handleDescribe() throws SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();

        var securityManager = SecurityManager.getInstance();
        securityManager.addPrincipal(Principal.getInstance(app, "password"));

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
    @DisplayName("Invalidate an active session")
    public void invalidateSession() throws SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();

        var securityManager = SecurityManager.getInstance();
        securityManager.addPrincipal(Principal.getInstance(app, "password"));

        var request = new Request(new Connect(app), app);
        this.signerService.handle(request);
        assertFalse(Session.getInstance(app).hasTimedOut());

        SessionManager.getInstance().invalidate(app);
        assertFalse(securityManager.hasPrincipal(app));
        assertThrows(SecurityManager.SecurityManagerException.class, () -> {
            Session.getInstance(app);
        });
    }

    @Test
    @DisplayName("Handle describe request without connect")
    public void handleDescribeWithoutConnect() {
        var app = Identity.generateRandomIdentity().getPublicKey();

        var securityManager = SecurityManager.getInstance();
        securityManager.addPrincipal(Principal.getInstance(app, "password"));

        var request = new Request(new Describe(), app);

        assertThrows(RuntimeException.class, () -> {
            this.signerService.handle(request);
        });
    }
}
