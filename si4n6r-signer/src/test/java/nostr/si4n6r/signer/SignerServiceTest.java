package nostr.si4n6r.signer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.NIP01;
import nostr.base.PublicKey;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.id.Identity;
import nostr.si4n6r.model.dto.MethodDto;
import nostr.si4n6r.model.dto.ParameterDto;
import nostr.si4n6r.model.dto.RequestDto;
import nostr.si4n6r.model.dto.ResponseDto;
import nostr.si4n6r.model.dto.SessionDto;
import nostr.si4n6r.rest.client.MethodRestClient;
import nostr.si4n6r.rest.client.ParameterRestClient;
import nostr.si4n6r.rest.client.RequestRestClient;
import nostr.si4n6r.rest.client.SessionManager;
import nostr.si4n6r.storage.common.ApplicationProxy;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        //MockitoAnnotations.openMocks(this);
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

        // Create the Session
        final var session = SignerServiceTest.createSession(user, app);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        // Get the Method
        var mClient = new MethodRestClient();
        var connectDto = mClient.getMethodById(MethodDto.MethodType.CONNECT.getId());
        if (connectDto == null) {
            throw new RuntimeException("Connect method not found");
        }

        // Create the Request
        var request = new RequestDto();
        request.setMethod(connectDto);
        request.setSession(session);
        request.setToken(session.getToken());
        request.setInitiator(app.toString());
        var rqclient = new RequestRestClient();
        log.log(Level.INFO, ">>>> Creating Request: {0}", request);
        var requestDto = rqclient.create(request);

        // Handle the connect method request
        var response = this.signerService.handle(requestDto);

        // Check the result
        var om = new ObjectMapper();
        var result = om.readValue(response.getResult(), SignerService.Result.class);
        assertEquals("ACK", result.getValue());
        Assertions.assertTrue(this.signerService.getSessionManager().sessionIsActive(app.toString()));

        // You cannot connectMethod twice
        Assertions.assertThrows(RuntimeException.class, () -> this.signerService.handle(requestDto));

        SessionManager.getInstance().deactivateSession(app.toString());
    }


    @Test
    @DisplayName("Handle the get_public_key method request")
    public void handleGetPublicKey() throws JsonProcessingException {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        // Create the Session
        final var session = SignerServiceTest.createSession(user, app);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        // Get the Method
        var mClient = new MethodRestClient();
        var connectDto = mClient.getMethodById(MethodDto.MethodType.CONNECT.getId());
        if (connectDto == null) {
            throw new RuntimeException("Connect method not found");
        }

        // Create the Request
        var request = new RequestDto();
        request.setMethod(connectDto);
        request.setSession(session);
        request.setToken(session.getToken());
        request.setInitiator(app.toString());
        var rqclient = new RequestRestClient();
        log.log(Level.INFO, ">>>> Creating Request: {0}", request);
        var requestDto = rqclient.create(request);

        // Handle the connect method request
        var response = this.signerService.handle(requestDto);

        var getPublicKeyDto = mClient.getMethodById(MethodDto.MethodType.GET_PUBLIC_KEY.getId());
        if (getPublicKeyDto == null) {
            throw new RuntimeException("get_public_key method not found");
        }

        request = new RequestDto();
        request.setMethod(getPublicKeyDto);
        request.setSession(session);
        request.setToken(session.getToken());
        request.setInitiator(app.toString());
        rqclient = new RequestRestClient();
        log.log(Level.INFO, ">>>> Creating Request: {0}", request);
        requestDto = rqclient.create(request);

        // Handle the connect method request
        response = this.signerService.handle(requestDto);

        // Check the result
        var om = new ObjectMapper();
        var result = om.readValue(response.getResult(), SignerService.Result.class);
        assertEquals(this.user.toString(), result.getValue());
    }

    @Test
    @DisplayName("Handle the ping method request")
    public void handlePing() throws JsonProcessingException {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();


        // Get the Method
        var mClient = new MethodRestClient();
        var pingDto = mClient.getMethodById(MethodDto.MethodType.PING.getId());
        if (pingDto == null) {
            throw new RuntimeException("Connect method not found");
        }

        // Create the Request
        var request = new RequestDto();
        request.setMethod(pingDto);
        request.setInitiator(app.toString());
        var rqclient = new RequestRestClient();
        log.log(Level.INFO, ">>>> Creating Request: {0}", request);
        var requestDto = rqclient.create(request);

        // Handle the connect method request
        var response = this.signerService.handle(requestDto);

        // Check the result
        var om = new ObjectMapper();
        var result = om.readValue(response.getResult(), SignerService.Result.class);
        assertEquals(ResponseDto.RESULT_PONG, result.getValue());
    }

    @Test
    @DisplayName("Handle the sign_event method request")
    public void testSignEvent() throws JsonProcessingException {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        // Create the Session
        final var session = SignerServiceTest.createSession(user, app);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        // Get the Method
        var mClient = new MethodRestClient();
        var connectDto = mClient.getMethodById(MethodDto.MethodType.CONNECT.getId());
        if (connectDto == null) {
            throw new RuntimeException("Connect method not found");
        }

        // Create the Request
        var request = new RequestDto();
        request.setMethod(connectDto);
        request.setSession(session);
        request.setToken(session.getToken());
        request.setInitiator(app.toString());

        var rqclient = new RequestRestClient();
        log.log(Level.INFO, ">>>> Creating Request: {0}", request);
        var requestDto = rqclient.create(request);

        // Handle the connect method request
        var response = this.signerService.handle(requestDto);

        var signEventDto = mClient.getMethodById(MethodDto.MethodType.SIGN_EVENT.getId());
        if (signEventDto == null) {
            throw new RuntimeException("sign_event method not found");
        }

        request = new RequestDto();
        request.setMethod(signEventDto);
        request.setSession(session);
        request.setToken(session.getToken());
        request.setInitiator(app.toString());

        rqclient = new RequestRestClient();
        log.log(Level.INFO, ">>>> Creating Request: {0}", request);
        requestDto = rqclient.create(request);

        var event = NIP01.createTextNoteEvent("Hello World, #si4n6r");
        var strEvent = new BaseEventEncoder(event).encode();

        var parameter = new ParameterDto();
        parameter.setRequest(requestDto);
        parameter.setName(ParameterDto.PARAM_EVENT);
        parameter.setValue(Base64.getEncoder().encodeToString(strEvent.getBytes(StandardCharsets.UTF_8)));

        var paramClient = new ParameterRestClient();
        log.log(Level.INFO, ">>> Creating parameter {0}", parameter);
        paramClient.create(parameter);

        // Handle the connect method request
        response = this.signerService.handle(requestDto);

        // Check the result
        var om = new ObjectMapper();
        var result = om.readValue(response.getResult(), SignerService.Result.class);
        var strSignedEvent = result.getValue();
        var decoder = new GenericEventDecoder(new String(Base64.getDecoder().decode(strSignedEvent), StandardCharsets.UTF_8));
        var signedEvent = decoder.decode();
        Assertions.assertNotNull(signedEvent.getSignature());
    }

    @Test
    @DisplayName("Handle the get_relays method request")
    public void testGetRelays() throws JsonProcessingException {
        this.app = Identity.generateRandomIdentity().getPublicKey();
        this.user = Identity.generateRandomIdentity().getPublicKey();

        // Create the Session
        final var session = SignerServiceTest.createSession(user, app);
        if (session == null) {
            throw new RuntimeException("Session not found");
        }

        // Get the Method
        var mClient = new MethodRestClient();
        var connectDto = mClient.getMethodById(MethodDto.MethodType.CONNECT.getId());
        if (connectDto == null) {
            throw new RuntimeException("Connect method not found");
        }

        // Create the Request
        var request = new RequestDto();
        request.setMethod(connectDto);
        request.setSession(session);
        request.setToken(session.getToken());
        request.setInitiator(app.toString());

        var rqclient = new RequestRestClient();
        log.log(Level.INFO, ">>>> Creating Request: {0}", request);
        var requestDto = rqclient.create(request);

        // Handle the connect method request
        var response = this.signerService.handle(requestDto);

        var getRelaysDto = mClient.getMethodById(MethodDto.MethodType.GET_RELAYS.getId());
        if (getRelaysDto == null) {
            throw new RuntimeException("get_relays method not found");
        }

        request = new RequestDto();
        request.setMethod(getRelaysDto);
        request.setSession(session);
        request.setToken(session.getToken());
        request.setInitiator(app.toString());

        rqclient = new RequestRestClient();
        log.log(Level.INFO, ">>>> Creating Request: {0}", request);
        requestDto = rqclient.create(request);

        // Handle the connect method request
        response = this.signerService.handle(requestDto);

        // Check the result
        var om = new ObjectMapper();
        var result = om.readValue(response.getResult(), SignerService.Result.class);
        var strRelays = result.getValue();
        List<String> relays = Arrays.stream(strRelays.split(",")).toList();
        assertEquals(1, relays.size());
        assertTrue(relays.contains("wss://relay.badgr.space"));
    }

    private ApplicationProxy createApplicationProxy(@NonNull String name, @NonNull PublicKey publicKey) {
        var result = new ApplicationProxy(publicKey);
        result.setName(name);
        return result;
    }

    private static SessionDto createSession(@NonNull PublicKey user, @NonNull PublicKey app) {
        return SessionManager.getInstance().createSession("bnnbn@badgr.space", user.toString(), app.toString(), 20 * 60, "password");
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
