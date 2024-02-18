package nostr.si4n6r.signer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.id.Identity;
import nostr.si4n6r.model.dto.MethodDto;
import nostr.si4n6r.model.dto.RequestDto;
import nostr.si4n6r.model.dto.SessionDto;
import nostr.si4n6r.rest.client.MethodRestClient;
import nostr.si4n6r.rest.client.RequestRestClient;
import nostr.si4n6r.rest.client.SessionManager;
import nostr.si4n6r.rest.client.SessionRestClient;
import nostr.si4n6r.storage.common.ApplicationProxy;
import org.junit.jupiter.api.*;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.logging.Level;

@Log
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SignerServiceTest {

    private SignerService signerService;
    private PublicKey app;
    private PublicKey user;
    //private SessionRestClient restClient;
    //private RestTemplate restTemplate;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.signerService = SignerService.getInstance();
        //this.restClient = new SessionRestClient();
        //this.restTemplate = new RestTemplate();
        //this.restClient.setRestTemplate(restTemplate);

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
    public void handleConnect() throws JsonProcessingException {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        // Session
        final var session = SignerServiceTest.createSession(user, app);
        //var sessionDto = restClient.create(session);

        if(session == null) {
            throw new RuntimeException("Session not found");
        }

        // Method
        var mClient = new MethodRestClient();
        var connectDto = mClient.getMethodById(MethodDto.MethodType.CONNECT.getId());

        if(connectDto == null) {
            throw new RuntimeException("Connect method not found");
        }

        // Request
        var request = new RequestDto();
        ResponseEntity<RequestDto> responseRequest = ResponseEntity.ok(request);
        request.setMethod(connectDto);
        //request.setRequestUuid(UUID.randomUUID().toString());
        request.setSession(session);
        request.setToken(session.getToken());
        request.setInitiator(app.toString());
        var rqclient = new RequestRestClient();
        log.log(Level.INFO, ">>>> Creating Request: {0}", request);
        var requestDto = rqclient.create(request);

        // Handle connect
        var response = this.signerService.handle(requestDto);

        var om = new ObjectMapper();
        var result = om.readValue(response.getResult(), SignerService.Result.class);
        Assertions.assertEquals("ACK", result.getValue());
        Assertions.assertTrue(this.signerService.getSessionManager().sessionIsActive(app.toString()));

        // You cannot connectMethod twice
        Assertions.assertThrows(RuntimeException.class, () -> this.signerService.handle(requestDto));

        SessionManager.getInstance().deactivateSession(app.toString());
    }

    @Test
    @DisplayName("Handle disconnect request without connect")
    public void handleDisconnectWithoutConnect() {
/*
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        //final var applicationProxy = createApplicationProxy("handleDisconnect", app);
        var session = SignerServiceTest.createSession(user, app);

        // You cannot disconnect without having connected first
        final var request = new Request<>(new Disconnect(app), session.getJwtToken());
        request.setInitiator(new ApplicationProxy(app));
        assertThrows(RuntimeException.class, () -> this.signerService.handle(request));
*/
    }

    @Test
    @DisplayName("Handle disconnect request")
    public void handleDisconnect() {
/*
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
*/
    }

    @Test
    @DisplayName("Handle describe request")
    public void handleDescribe() {
/*
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
*/
    }

    @Test
    @DisplayName("Deactivate an active session")
    public void deactivateSession() {
/*
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
*/
    }

    @Test
    @DisplayName("Handle describe request without connect")
    public void handleDescribeWithoutConnect() {
/*
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        var session = SignerServiceTest.createSession(user, app);
        var request = new Request<>(new Describe(), session.getJwtToken());
        request.setInitiator(new ApplicationProxy(app));
*/
    }

    private ApplicationProxy createApplicationProxy(@NonNull String name, @NonNull PublicKey publicKey) {
        var result = new ApplicationProxy(publicKey);
        result.setName(name);
        return result;
    }

    private static SessionDto createSession(@NonNull PublicKey user, @NonNull PublicKey app) {
        return SessionManager.getInstance().createSession(user.toString(), app.toString(), 20 * 60, "password", "secret");
    }

    private static SessionDto getSessionDto(SessionDto expectedEntity) {
        var sessionDto = new SessionDto();
        sessionDto.setSessionId(expectedEntity.getSessionId());
        sessionDto.setToken(expectedEntity.getToken());
        sessionDto.setApp(expectedEntity.getApp());
        sessionDto.setAccount(expectedEntity.getAccount());
        sessionDto.setStatus(expectedEntity.getStatus());
        sessionDto.setCreatedAt(expectedEntity.getCreatedAt());
        sessionDto.setId(expectedEntity.getId());
        return sessionDto;
    }

}
