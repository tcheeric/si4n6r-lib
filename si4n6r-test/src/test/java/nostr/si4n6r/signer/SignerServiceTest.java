package nostr.si4n6r.signer;

import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.id.Identity;
import nostr.si4n6r.core.IMethod;
import nostr.si4n6r.core.impl.ApplicationProxy;
import nostr.si4n6r.core.impl.Request;
import nostr.si4n6r.core.impl.Session;
import nostr.si4n6r.core.impl.SessionManager;
import nostr.si4n6r.signer.methods.Connect;
import nostr.si4n6r.signer.methods.Describe;
import nostr.si4n6r.signer.methods.Disconnect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SignerServiceTest {

    private SignerService signerService;
    private PublicKey app;
    private PublicKey user;

    @BeforeAll
    public void setUp() {
        this.signerService = SignerService.getInstance(Relay.fromString("wss://relay.badgr.space"));
    }

    @Test
    @DisplayName("Signer-Initiated connect")
    public void connect() {
/*
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
*/
    }

    @Test
    @DisplayName("Handle app-initiated connect request")
    public void handleConnect() {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

/*
        final var applicationProxy = createApplicationProxy("handleConnect", app);
*/
        final var session = SignerServiceTest.createSession(user, app);

        assertTrue(this.signerService.getSessionManager().sessionIsNew(app));

        final var request = new Request<>(new Connect(app), session.getJwtToken());
        request.setInitiator(new ApplicationProxy(app));
        this.signerService.handle(request);

        assertEquals("ACK", request.getMethod().getResult());
        assertTrue(this.signerService.getSessionManager().sessionIsActive(app));

        // You cannot connect twice
        assertThrows(RuntimeException.class, () -> this.signerService.handle(request));

        SessionManager.getInstance().deactivateSession(app);
    }

    @Test
    @DisplayName("Handle disconnect request without connect")
    public void handleDisconnectWithoutConnect() {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        //final var applicationProxy = createApplicationProxy("handleDisconnect", app);
        var session = SignerServiceTest.createSession(user, app);

        // You cannot disconnect without having connected first
        final var request = new Request<>(new Disconnect(app), session.getJwtToken());
        request.setInitiator(new ApplicationProxy(app));
        assertThrows(RuntimeException.class, () -> this.signerService.handle(request));
    }

    @Test
    @DisplayName("Handle disconnect request")
    public void handleDisconnect() {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        //final var applicationProxy = createApplicationProxy("handleDisconnect", app);
        var session = SignerServiceTest.createSession(user, app);

        var request = new Request<>(new Connect(app), session.getJwtToken());
        request.setInitiator(new ApplicationProxy(app));
        this.signerService.handle(request);

        request = new Request<>(new Disconnect(app), session.getJwtToken());
        request.setInitiator(new ApplicationProxy(app));
        this.signerService.handle(request);

        assertEquals("ACK", request.getMethod().getResult());
        assertTrue(this.signerService.getSessionManager().sessionIsInactive(app));
    }

    @Test
    @DisplayName("Handle describe request")
    public void handleDescribe() {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        var session = SignerServiceTest.createSession(user, app);

        var request = new Request<>(new Connect(app), session.getJwtToken());
        request.setInitiator(new ApplicationProxy(app));
        this.signerService.handle(request);


        var requestDescribe = new Request<>(new Describe(), session.getJwtToken());
        requestDescribe.setInitiator(new ApplicationProxy(app));
        this.signerService.handle(requestDescribe);

        var result = requestDescribe.getMethod().getResult();
        assertEquals(3, result.size());
        assertTrue(result.contains(IMethod.Constants.METHOD_CONNECT));
        assertTrue(result.contains(IMethod.Constants.METHOD_DISCONNECT));
        assertTrue(result.contains(IMethod.Constants.METHOD_DESCRIBE));
    }

    @Test
    @DisplayName("Deactivate an active session")
    public void deactivateSession() {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        var session = SignerServiceTest.createSession(user, app);

        var request = new Request<>(new Connect(app), session.getJwtToken());
        request.setInitiator(new ApplicationProxy(app));
        this.signerService.handle(request);

        session = SessionManager.getInstance().getSession(app);
        assertFalse(session.hasExpired());

        SessionManager.getInstance().deactivateSession(app);
        assertTrue(SessionManager.getInstance().sessionIsInactive(app));
    }

    @Test
    @DisplayName("Handle describe request without connect")
    public void handleDescribeWithoutConnect() {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        var session = SignerServiceTest.createSession(user, app);
        var request = new Request<>(new Describe(), session.getJwtToken());
        request.setInitiator(new ApplicationProxy(app));
    }

    private ApplicationProxy createApplicationProxy(@NonNull String name, @NonNull PublicKey publicKey) {
        var result = new ApplicationProxy(publicKey);
        result.setName(name);
        return result;
    }

    private static Session createSession(@NonNull PublicKey user, @NonNull PublicKey app) {
        return SessionManager.getInstance().createSession(user, app, 20*60, "password", "secret");
    }
}
