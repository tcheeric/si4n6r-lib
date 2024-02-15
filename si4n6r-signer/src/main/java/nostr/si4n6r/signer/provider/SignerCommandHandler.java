package nostr.si4n6r.signer.provider;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.NIP04;
import nostr.api.NIP46;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.GenericEvent;
import nostr.si4n6r.model.Method;
import nostr.si4n6r.model.Parameter;
import nostr.si4n6r.model.Request;
import nostr.si4n6r.model.Session;
import nostr.si4n6r.model.dto.MethodDto;
import nostr.si4n6r.model.dto.ParameterDto;
import nostr.si4n6r.model.dto.RequestDto;
import nostr.si4n6r.model.dto.ResponseDto;
import nostr.si4n6r.model.dto.SessionDto;
import nostr.si4n6r.rest.client.RequestRestClient;
import nostr.si4n6r.rest.client.SessionManager;
import nostr.si4n6r.signer.Signer;
import nostr.si4n6r.signer.SignerService;
import nostr.si4n6r.util.Util;
import nostr.util.NostrException;
import nostr.ws.handler.command.spi.ICommandHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

import static nostr.api.Nostr.Json.decodeEvent;

@Log
public class SignerCommandHandler implements ICommandHandler {

    @Override
    public void onEose(String subId, Relay relay) {
        log.log(Level.FINER, "onEose({0}, {1})", new Object[]{subId, relay});
        // TODO
    }

    @Override
    public void onOk(String eventId, String reasonMessage, Reason reason, boolean result, Relay relay) {
        log.log(Level.FINER, "onOk({0}, {1}, {2}, {3}, {4})", new Object[]{eventId, reasonMessage, reason, result, relay});
        // TODO
    }

    @Override
    public void onNotice(String message) {
        log.log(Level.FINER, "onNotice({0})", message);
        // TODO
    }

    @Override
    public void onEvent(String jsonEvent, String subId, Relay relay) {

        log.log(Level.FINE, "Received event {0} with subscription id {1} from relay {2}", new Object[]{jsonEvent, subId, relay});

        var event = decodeEvent(jsonEvent);
        log.log(Level.FINER, "Decoded event: {0}", event);

        // TODO
        var signer = Signer.getInstance();
        log.log(Level.FINE, "Signer: {0}", signer.getIdentity().getPublicKey());
        var app = event.getPubKey(); // Application.appIdentity
        var recipient = Util.getEventRecipient(event); // Signer

        // TODO - Also make sure the public key is a registered/known pubkey, and ignore all other pubkeys
        log.log(Level.FINE, "Recipient: {0} - Signer: {1}", new Object[]{recipient, signer.getIdentity().getPublicKey()});
        if (event.getKind() == 24133 && recipient.equals(signer.getIdentity().getPublicKey())) {
            handleKind24133(event, signer, app);
        } /*else if (event.getKind() == 4) {
            handleKind4(event, signer);
        } */ else {
            log.log(Level.FINE, "Skipping event {0} with nip {1}. All fine!", new Object[]{event, event.getNip()});
        }
    }

    @Override
    public void onAuth(String challenge, Relay relay) {
        log.log(Level.FINER, "onAuth({0}, {1})", new Object[]{challenge, relay});
    }

    private static SessionDto toSessionDto(@NonNull NIP46.Session session) {
        var sessionDto = new SessionDto();
        sessionDto.setSessionId(session.getSessionId());
        sessionDto.setCreatedAt(session.getCreatedAt());
        sessionDto.setToken(session.getToken());
        sessionDto.setApp(session.getApp());
        sessionDto.setStatus(sessionDto.getStatus());

        return sessionDto;
    }

    private static ResponseDto toResponseDto(@NonNull NIP46.Response response) {
        var responseDto = new ResponseDto();
        responseDto.setResponseUuid(response.getResponseUuid());
        responseDto.setResult(response.getResult());
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setCreatedAt(response.getCreatedAt());
        responseDto.setSession(toSessionDto(response.getSession()));

        return responseDto;
    }

    private static RequestDto toRequest(NIP46.Request nip46Request) {
        var request = new RequestDto();
        request.setRequestUuid(nip46Request.getRequestUuid());
        request.setToken(nip46Request.getToken());
        request.setInitiator(nip46Request.getInitiator());
        request.setSession(toSessionDto(nip46Request.getSession()));
        request.setCreatedAt(request.getCreatedAt());
        request.setToken(request.getToken());
        request.setMethod(toMethod(nip46Request.getMethod()));
        return request;
    }

    private static MethodDto toMethod(@NonNull NIP46.Method method) {
        MethodDto methodDto = new MethodDto();
        methodDto.setDescription(method.getDescription());
        methodDto.setName(method.getName());

        return methodDto;
    }

    private static ParameterDto toParameterDto(NIP46.Parameter parameter) {
        var parameterDto = new ParameterDto();
        parameterDto.setName(parameter.getName());
        parameterDto.setValue(parameter.getValue());

        return parameterDto;
    }

    private static PublicKey getPublicKey(@NonNull String hex) {
        return new PublicKey(hex);
    }

    private void handleKind24133(GenericEvent event, Signer signer, PublicKey app) throws RuntimeException {
        log.log(Level.INFO, "Processing event {0}", event);

        String content;
        try {
            content = NIP04.decrypt(signer.getIdentity(), event);
        } catch (NostrException e) {
            throw new RuntimeException(e);
        }
        log.log(Level.INFO, "Content: {0}", content);

        if (content != null) {
            var nip46Request = NIP46.Request.fromString(content);
            var request = toRequest(nip46Request);
            var params = getParameters(nip46Request);
            var sessionManager = SessionManager.getInstance();

            log.log(Level.INFO, "Request: {0}", request);
            log.log(Level.INFO, "Method: {0}", request.getMethod());

            var restClient = new RequestRestClient();
            restClient.create(request);

            sessionManager.addRequest(request, app.toString());
            var service = SignerService.getInstance();
            var response = service.handle(request);

            log.log(Level.INFO, "Response: {0}", response);
        }
    }

    private Set<ParameterDto> getParameters(NIP46.Request nip46Request) {
        var request = Request.fromNIP46Request(nip46Request);
        var parameters = request.getParameters();
        var result = new LinkedHashSet<ParameterDto>();
        parameters.stream().map(p -> fromParameter(p)).forEach(result::add);
        return result;
    }

    private static ParameterDto fromParameter(@NonNull Parameter parameter) {
        var parameterDto = new ParameterDto();
        parameterDto.setName(parameter.getName());
        parameterDto.setValue(parameter.getValue());
        parameterDto.setRequest(fromRequest(parameter.getRequest()));
        return parameterDto;
    }

    private static RequestDto fromRequest(@NonNull Request request) {
        var requestDto = new RequestDto();
        requestDto.setRequestUuid(request.getRequestUuid().toString());
        requestDto.setToken(request.getToken());
        requestDto.setInitiator(request.getInitiator());
        requestDto.setCreatedAt(request.getCreatedAt());
        requestDto.setSession(fromSession(request.getSession()));
        requestDto.setMethod(fromMethod(request.getMethod()));
        return requestDto;
    }

    private static SessionDto fromSession(@NonNull Session session) {
        var sessionDto = new SessionDto();
        sessionDto.setSessionId(session.getSessionId().toString());
        sessionDto.setApp(session.getApp());
        sessionDto.setAccount(session.getAccount());
        sessionDto.setCreatedAt(session.getCreatedAt());
        sessionDto.setToken(session.getToken());
        sessionDto.setStatus(session.getStatus());
        return sessionDto;
    }

    private static MethodDto fromMethod(@NonNull Method method) {
        var methodDto = new MethodDto();
        methodDto.setName(method.getName());
        methodDto.setDescription(method.getDescription());
        return methodDto;
    }

}
