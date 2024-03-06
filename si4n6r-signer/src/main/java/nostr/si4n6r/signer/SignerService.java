package nostr.si4n6r.signer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.NIP01;
import nostr.api.NIP04;
import nostr.api.NIP46;
import nostr.api.Nostr;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.id.IIdentity;
import nostr.id.Identity;
import nostr.si4n6r.model.dto.MethodDto;
import nostr.si4n6r.model.dto.ParameterDto;
import nostr.si4n6r.model.dto.RequestDto;
import nostr.si4n6r.model.dto.ResponseDto;
import nostr.si4n6r.model.dto.SessionDto;
import nostr.si4n6r.rest.client.ParameterRestClient;
import nostr.si4n6r.rest.client.RequestRestClient;
import nostr.si4n6r.rest.client.ResponseRestClient;
import nostr.si4n6r.rest.client.SessionManager;
import nostr.si4n6r.storage.Vault;
import nostr.si4n6r.storage.common.AccountProxy;
import nostr.si4n6r.storage.fs.NostrAccountFSVault;
import nostr.si4n6r.util.JWTUtil;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Data
@Log
public class SignerService {

    private static SignerService instance;
    private final Signer signer;
    private final SessionManager sessionManager;

    private SignerService() {
        this.signer = Signer.getInstance();
        this.sessionManager = SessionManager.getInstance();
    }

    private SignerService(@NonNull Relay relay) {
        this.signer = Signer.getInstance(relay);
        this.sessionManager = SessionManager.getInstance();
    }

    public static SignerService getInstance() {
        if (instance == null) {
            instance = new SignerService();
        }
        return instance;
    }

    public static SignerService getInstance(@NonNull Relay relay) {
        if (instance == null) {
            instance = new SignerService(relay);
        }
        return instance;
    }

    /**
     * Signer-initiated connection to an application.
     *
     * @param app the application to connect to
     */
/*    public void doConnect(@NonNull ApplicationProxy app) {

        final PublicKey appPublicKey = new PublicKey(app.getPublicKey());
        IMethod<String> connect = new Connect(appPublicKey);
        var request = new Request<>(connect, app);
        request.setSessionId(sessionManager.createSession(appPublicKey).getId());

        sessionManager.addRequest(request, appPublicKey);

        List<String> params = new ArrayList<>();
        params.add(app.toString());

        log.log(Level.INFO, "Submitting request {0}", request);
        var event = NIP46.createRequestEvent(new NIP46.NIP46Request(request.getId(), METHOD_CONNECT, params, request.getSessionId()), signer.getIdentity(), appPublicKey);

        Nostr.sign(signer.getIdentity(), event);
        Nostr.send(event);

    }*/

    /**
     * Handling app-initiated requests and submit a corresponding response back.
     *
     * @param request the request to handle and respond to.
     */
    public ResponseDto handle(@NonNull RequestDto request) {
        var method = request.getMethod().getName();
        var app = request.getInitiator();

        log.log(Level.INFO, "Handling {0}", request);

        if (request.getId() == null) {
            var requestRestClient = new RequestRestClient();
            request = requestRestClient.create(request);
        }

        // Some requests do not need a session (ping, create_session)
        if (request.getSession() != null) {
            validateSession(request);
        }

        ResponseDto response;
        var sender = signer.getIdentity();

        switch (method) {
            case "describe" -> response = handleDescribe(request, sender);
            case "disconnect" -> response = handleDisconnect(request, sender);
            case "connect" -> response = handleConnect(request, sender);
            case "get_public_key" -> response = handleGetPublicKey(request, sender);
            case "sign_event" -> response = handleSignEvent(request, sender);
            case "ping" -> response = handlePing(request, sender);
            case "get_relays" -> response = handleGetRelays(request, sender);
            case "nip04_encrypt" -> response = handleNip04Encrypt(request, sender);
            case "nip04_decrypt" -> response = handleNip04Decrypt(request, sender);
            case "nip44_encrypt" -> response = handleNip44Encrypt(request, sender);
            case "nip44_decrypt" -> response = handleNip44Decrypt(request, sender);
            default -> throw new RuntimeException("Invalid request: " + request);
        }

        sessionManager.addResponse(response, app);
        sessionManager.addRequest(request, app);

        return response;
    }

    private ResponseDto handleNip44Decrypt(RequestDto request, IIdentity sender) {
        var result = doGetNip44Decrypt(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto handleNip44Encrypt(RequestDto request, IIdentity sender) {
        var result = doGetNip44Encrypt(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto handleNip04Decrypt(RequestDto request, IIdentity sender) {
        var result = doGetNip04Decrypt(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto handleNip04Encrypt(RequestDto request, IIdentity sender) {
        var result = doGetNip04Encrypt(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto handleGetRelays(RequestDto request, IIdentity sender) {
        var result = doGetRelays(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto handlePing(RequestDto request, IIdentity sender) {
        var result = doPing(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto handleSignEvent(RequestDto request, IIdentity sender) {
        var result = doSignEvent(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto handleGetPublicKey(RequestDto request, IIdentity sender) {
        var result = doGetPublicKey(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto handleConnect(RequestDto request, IIdentity sender) {
        var result = doConnect(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto handleDescribe(RequestDto request, IIdentity sender) {
        var result = doDescribe(request.getInitiator());
        return createResponse(request, result, sender);
    }

    private ResponseDto handleDisconnect(RequestDto request, IIdentity sender) {
        var result = doDisconnect(request);
        return createResponse(request, result, sender);
    }

    private ResponseDto createResponse(RequestDto request, Result result, IIdentity sender) {
        var response = new ResponseDto(request);
        response.setResult(Result.toJson(result));
        var restClient = new ResponseRestClient();
        response = restClient.create(response);

        var event = NIP46.createResponseEvent(
                toResponse(response),
                sender,
                new PublicKey(request.getInitiator())
        );

        log.log(Level.FINE, "Submitting event {0} to relay(s)", event);
        Nostr.sign(sender, event);
        Nostr.send(event);

        return response;
    }

    private String getDomain(@NonNull String name) {
        return name.split("@")[1];
    }

    private Result doGetNip44Decrypt(@NonNull RequestDto request) {
        return null;
    }

    private Result doGetNip44Encrypt(@NonNull RequestDto request) {
        return null;
    }

    private Result doGetNip04Decrypt(@NonNull RequestDto request) {
        log.log(Level.INFO, "Decrypting nip04: {0}", request);
        var client = new ParameterRestClient();
        var params = client.getParametersByRequest(request);
        var paramPlainText = params.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName().equals(ParameterDto.PARAM_CIPHERTEXT))
                .findFirst();
        var paramRcptPublicKey = params.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName().equals(ParameterDto.PARAM_PUBLIC_KEY))
                .findFirst();

        if (paramPlainText.isPresent() && paramRcptPublicKey.isPresent()) {
            var plaintext = paramPlainText.get().getValue();
            var result = new Result(request.getInitiator());
            var jwtUtil = new JWTUtil(request.getToken());
            var password = jwtUtil.getPassword();
            var account = new AccountProxy();
            Vault<AccountProxy> vault = new NostrAccountFSVault();

            account.setId(jwtUtil.getNip05());
            account.setPublicKey(jwtUtil.getSubject());
            var nsec = vault.retrieve(account, password);
            Identity id = Identity.getInstance(new PrivateKey(nsec));
            result.setValue(NIP04.decrypt(id, plaintext, new PublicKey(paramRcptPublicKey.get().getValue())));
            return result;
        }

        return new Result();
    }

    private Result doGetNip04Encrypt(@NonNull RequestDto request) {
        log.log(Level.INFO, "Encrypting nip04: {0}", request);
        var client = new ParameterRestClient();
        var params = client.getParametersByRequest(request);
        var paramPlainText = params.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName().equals(ParameterDto.PARAM_PLAINTEXT))
                .findFirst();
        var paramRcptPublicKey = params.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName().equals(ParameterDto.PARAM_PUBLIC_KEY))
                .findFirst();

        if (paramPlainText.isPresent() && paramRcptPublicKey.isPresent()) {
            var plaintext = paramPlainText.get().getValue();
            var result = new Result(request.getInitiator());
            var jwtUtil = new JWTUtil(request.getToken());
            var password = jwtUtil.getPassword();
            var account = new AccountProxy();
            Vault<AccountProxy> vault = new NostrAccountFSVault();

            account.setId(jwtUtil.getNip05());
            account.setPublicKey(jwtUtil.getSubject());
            var nsec = vault.retrieve(account, password);
            Identity id = Identity.getInstance(new PrivateKey(nsec));
            result.setValue(NIP04.encrypt(id, plaintext, new PublicKey(paramRcptPublicKey.get().getValue())));
            return result;
        }

        return new Result();
    }

    private IEvent getEvent(@NonNull String strEvent) {
        var decodedEvent = Base64.getDecoder().decode(strEvent.getBytes(StandardCharsets.UTF_8));
        var decoder = new GenericEventDecoder(new String(decodedEvent, StandardCharsets.UTF_8));
        return decoder.decode();
    }

    private void validateSession(@NonNull RequestDto request) {
        var app = request.getInitiator();
        var method = request.getMethod().getName();
        log.log(Level.INFO, "Validating session for {0} using method {1}", new Object[]{app, method});

        SessionDto session;
        boolean hasSession;
        if (MethodDto.MethodType.CONNECT.getName().equals(method)) {
            session = sessionManager.getNewSession(app);
            hasSession = this.sessionManager.sessionIsNew(app);
        } else {
            session = sessionManager.getActiveSession(app);
            hasSession = this.sessionManager.sessionIsActive(app);
        }

        var token = request.getToken();

        if (!session.getToken().equals(token)) {
            throw new RuntimeException(String.format("Failed validation: Invalid session id %s for %s.", token, app));
        }

        if (hasSession && token == null) {
            throw new RuntimeException(String.format("Failed validation: Missing session id for %s.", app));
        }
    }

    private Result doDescribe(@NonNull String app) {
        // Get all method types from the MethodDto.MethodType enum
        List<String> methodList = Arrays.stream(MethodDto.MethodType.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        log.log(Level.INFO, "describe: {0}", methodList);

        var result = new Result(app);
        // Join the method types into a comma-separated string
        result.setValue(String.join(", ", methodList));

        return result;
    }

    private Result doConnect(@NonNull RequestDto requestDto) {
        var app = new PublicKey(requestDto.getInitiator());

        if (isConnected(app)) {
            throw new RuntimeException(String.format("Cannot connect: %s is already connected!", app));
        }

        this.sessionManager.activateSession(app.toString());
        log.log(Level.INFO, "ACK: {0} connected!", app);

        var result = new Result(app.toString());
        result.setValue(ResponseDto.RESULT_ACK);
        return result;
    }

    private Result doPing(@NonNull RequestDto request) {
        var result = new Result();
        result.setApp(request.getInitiator());
        result.setValue(ResponseDto.RESULT_PONG);
        return result;
    }

    private Result doGetRelays(@NonNull RequestDto request) {
        var token = request.getToken();
        var jwtUtil = new JWTUtil(token);
        var nip05 = jwtUtil.getNip05();
        var relays = getRelays(nip05);

        var result = new Result(request.getInitiator());
        result.setValue(String.join(", ", relays));
        return result;
    }

    private Set<String> getRelays(@NonNull String nip05) {
        String url = createUrl(nip05);

        // Make a GET request and parse the response to a Map
        var restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        log.log(Level.INFO, "Relays response: {0}", response);

        // Extract the "relays" object
        var tmpResp = Base64.getDecoder().decode(response.get("result").toString());
        var objectMapper = new ObjectMapper();
        Map<String, Object> responseMap;
        try {
            responseMap = objectMapper.readValue(tmpResp, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Extract the "relays" object
        Map<String, List<String>> relays = (Map<String, List<String>>) responseMap.get("relays");

        log.log(Level.INFO, "Relays: {0}", relays);

        // Create a set to store the relay URLs
        Set<String> relayUrls = new HashSet<>();

        // Iterate over the "relays" object and add all the relay URLs to the set
        if (relays != null) {
            for (List<String> urls : relays.values()) {
                relayUrls.addAll(urls);
            }
        }
        return relayUrls;
    }

    private String createUrl(@NonNull String nip05) {
        var local = nip05.split("@")[0];
        var domain = nip05.split("@")[1];
        return "http://localhost:8080/bottin/nip05?localpart=" + local + "&domain=" + domain;
    }

    private Result doSignEvent(@NonNull RequestDto requestDto) {
        log.log(Level.INFO, "Signing event: {0}", requestDto);
        var client = new ParameterRestClient();
        var params = client.getParametersByRequest(requestDto);
        var param = params.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName().equals(ParameterDto.PARAM_EVENT))
                .findFirst();

        if (param.isPresent()) {
            var strEvent = param.get().getValue();
            IEvent event = getEvent(strEvent);
            NIP01.sign((ISignable) event);
            strEvent = new BaseEventEncoder((GenericEvent) event).encode();

            log.log(Level.INFO, "Signed event: {0}", strEvent);

            var result = new Result(requestDto.getInitiator());
            result.setValue(Base64.getEncoder().encodeToString(strEvent.getBytes()));
            return result;
        }

        return new Result();
    }

    private Result doGetPublicKey(@NonNull RequestDto requestDto) {
        var jwtUtil = new JWTUtil(requestDto.getToken());
        var result = new Result(requestDto.getInitiator());
        result.setValue(jwtUtil.getSubject());
        return result;
    }

    private Result doDisconnect(@NonNull RequestDto requestDto) {
        var app = new PublicKey(requestDto.getInitiator());
        if (!isConnected(app)) {
            throw new RuntimeException(String.format("Failed to disconnect: %s is not connected!", app));
        }
        this.sessionManager.deactivateSession(app.toString());
        log.log(Level.INFO, "ACK: {0} disconnected!", app);

        Result result = new Result(app.toString());
        result.setValue(ResponseDto.RESULT_ACK);

        return result;
    }

    private NIP46.Request toRequest(RequestDto requestDto) {
        var request = new NIP46.Request();

        request.setRequestUuid(requestDto.getRequestUuid());
        request.setToken(requestDto.getToken());
        request.setInitiator(requestDto.getInitiator());
        request.setSession(toSession(requestDto.getSession()));
        request.setCreatedAt(requestDto.getCreatedAt());
        request.setToken(requestDto.getToken());
        request.setMethod(toMethod(requestDto.getMethod()));

        return request;
    }

    private NIP46.Response toResponse(ResponseDto responseDto) {
        var response = new NIP46.Response();
        response.setResponseUuid(responseDto.getResponseUuid());
        response.setResult(responseDto.getResult());
        if (responseDto.getSession() != null) {
            response.setSession(toSession(responseDto.getSession()));
        }
        return response;
    }

    private NIP46.Session toSession(SessionDto sessionDto) {
        var session = new NIP46.Session();
        session.setSessionId(sessionDto.getSessionId());
        session.setCreatedAt(sessionDto.getCreatedAt());
        session.setToken(sessionDto.getToken());
        session.setApp(sessionDto.getApp());
        session.setStatus(sessionDto.getStatus());
        return session;
    }

    private NIP46.Method toMethod(MethodDto methodDto) {
        var method = new NIP46.Method();
        method.setName(methodDto.getName());
        method.setDescription(methodDto.getDescription());
        return method;
    }

    private NIP46.Parameter toParameter(ParameterDto parameterDto) {
        var parameter = new NIP46.Parameter();
        parameter.setName(parameterDto.getName());
        parameter.setValue(parameterDto.getValue());
        return parameter;
    }

    // TODO - Implement the method. Check is a connection already exists for the app.
    private boolean isConnected(@NonNull PublicKey app) {
        SessionDto session = SessionManager.getInstance().getSession(app.toString());

        if (session == null) {
            return false;
        }

        if (!session.getStatus().equals(SessionDto.STATUS_ACTIVE)) {
            return false;
        }

        var reqRestClient = new RequestRestClient();
        var requests = reqRestClient.getBySessionIdByMethodId(session.getId(), MethodDto.MethodType.CONNECT.getId());

        var req = requests.stream().findFirst();

        if (req.isEmpty()) {
            return false;
        }

        var respRestClient = new ResponseRestClient();
        var resp = respRestClient.getByResponseUuid(req.get().getRequestUuid());

        var om = new ObjectMapper();
        Result result;
        try {
            result = om.readValue(resp.getResult(), Result.class);
        } catch (JsonProcessingException e) {
            log.log(Level.WARNING, "Failed to parse response: {0}", e.getMessage());
            return false;
        }
        return result.getValue().equals(ResponseDto.RESULT_ACK);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {
        private String value;
        private String message;
        private String sessionId;
        private String app;

        public Result(@NonNull String app) {
            this.app = app;
            SessionManager sessionManager = SessionManager.getInstance();
            var session = sessionManager.getSession(app);
            if (session == null) {
                throw new RuntimeException("No session found for app: " + app);
            }
            this.sessionId = session.getSessionId();
        }

        public String toJson() {
            return String.format("{\"value\": \"%s\", \"message\": \"%s\", \"sessionId\": \"%s\", \"app\": \"%s\"}", value, message, sessionId, app);
        }

        public static String toJson(Result result) {
            return result.toJson();
        }

        public static Result fromJson(String json) {
            var om = new ObjectMapper();
            try {
                return om.readValue(json, Result.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
