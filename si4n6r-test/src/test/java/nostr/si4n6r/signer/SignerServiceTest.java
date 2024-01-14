package nostr.si4n6r.signer;

import nostr.base.Relay;
import nostr.id.Identity;
import nostr.si4n6r.core.IMethod;
import nostr.si4n6r.core.impl.*;
import nostr.si4n6r.core.impl.SecurityManager;
import nostr.si4n6r.signer.methods.Connect;
import nostr.si4n6r.signer.methods.Describe;
import nostr.si4n6r.signer.methods.Disconnect;
import org.junit.jupiter.api.*;

import lombok.NonNull;
import nostr.base.PublicKey;

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
    public void connect() throws SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();
        SecurityManager.getInstance().addPrincipal(Principal.getInstance(app, "password"));
        this.signerService.doConnect(new ApplicationProxy(app));

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
        final ApplicationProxy applicationProxy = createApplicationProxy("handleConnect", app);

        var request = new Request<>(new Connect(app), applicationProxy);
        this.signerService.handle(request);

        assertEquals("ACK", request.getMethod().getResult());
        assertTrue(this.signerService.getSessionManager().hasActiveSession(app));
    }

    @Test
    @DisplayName("Handle app-initiated connect request without passwrd being provided")
    public void handleConnectWithoutPassword() {
        var app = Identity.generateRandomIdentity().getPublicKey();
        final ApplicationProxy applicationProxy = createApplicationProxy("handleConnectWithoutPassword", app);

        var request = new Request<>(new Connect(app), applicationProxy);

        assertThrows(SecurityManager.SecurityManagerException.class, () -> this.signerService.handle(request));
    }

    @Test
    @DisplayName("Handle disconnect request")
    public void handleDisconnect() throws SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();

        var securityManager = SecurityManager.getInstance();
        securityManager.addPrincipal(Principal.getInstance(app, "password"));
        final ApplicationProxy applicationProxy = createApplicationProxy("handleDisconnect", app);

        var request = new Request<>(new Connect(app), applicationProxy);
        this.signerService.handle(request);
        assertFalse(Session.getInstance(app).hasTimedOut());

        request = new Request<>(new Disconnect(), applicationProxy);
        var session = this.signerService.getSessionManager().getSession(app);
        request.setSessionId(session.getId());
        this.signerService.handle(request);

        assertEquals("ACK", request.getMethod().getResult());
        assertThrows(SecurityManager.SecurityManagerException.class, () -> Session.getInstance(app));
    }

    @Test
    @DisplayName("Handle describe request")
    public void handleDescribe() throws SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();

        var securityManager = SecurityManager.getInstance();
        securityManager.addPrincipal(Principal.getInstance(app, "password"));
        final ApplicationProxy applicationProxy = createApplicationProxy("handleDescribe", app);

        var requestConnect = new Request<>(new Connect(app), applicationProxy);
        this.signerService.handle(requestConnect);
        assertFalse(Session.getInstance(app).hasTimedOut());

        var describe = new Describe();
        var requestDescribe = new Request<>(describe, applicationProxy);
        var session = this.signerService.getSessionManager().getSession(app);
        requestDescribe.setSessionId(session.getId());
        this.signerService.handle(requestDescribe);

        var result = requestDescribe.getMethod().getResult();
        assertEquals(3, result.size());
        assertTrue(result.contains(IMethod.Constants.METHOD_CONNECT));
        assertTrue(result.contains(IMethod.Constants.METHOD_DISCONNECT));
        assertTrue(result.contains(IMethod.Constants.METHOD_DESCRIBE));
    }

    @Test
    @DisplayName("Invalidate an active session")
    public void invalidateSession() throws SecurityManager.SecurityManagerException {
        var app = Identity.generateRandomIdentity().getPublicKey();

        var securityManager = SecurityManager.getInstance();
        securityManager.addPrincipal(Principal.getInstance(app, "password"));

        var request = new Request<>(new Connect(app), createApplicationProxy("invalidateSession", app));
        this.signerService.handle(request);
        assertFalse(Session.getInstance(app).hasTimedOut());

        SessionManager.getInstance().invalidate(app);
        assertFalse(securityManager.hasPrincipal(app, "password"));
        assertThrows(SecurityManager.SecurityManagerException.class, () -> Session.getInstance(app));
    }

    @Test
    @DisplayName("Handle describe request without connect")
    public void handleDescribeWithoutConnect() {
        var app = Identity.generateRandomIdentity().getPublicKey();

        var securityManager = SecurityManager.getInstance();
        securityManager.addPrincipal(Principal.getInstance(app, "password"));

        var request = new Request<>(new Describe(), createApplicationProxy("handleDescribeWithoutConnect", app));

        assertThrows(RuntimeException.class, () -> this.signerService.handle(request));
    }

    private ApplicationProxy createApplicationProxy(String name, @NonNull PublicKey publicKey) {
        var result = new ApplicationProxy(publicKey);
        result.setName(name);
        return result;
    }
}
