package nostr.si4n6r.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nostr.api.NIP46;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "Session")
@Table(name = "t_session")
public class Session {

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    public Session() {
        this.createdAt = LocalDateTime.now();
        this.sessionId = UUID.randomUUID().toString();
        this.status = STATUS_NEW;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @NotNull
    @Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
    private String status;

    @NotNull
    @Column(name = "account", nullable = false, length = Integer.MAX_VALUE)
    private String account;

    @NotNull
    @Column(name = "app", nullable = false, length = Integer.MAX_VALUE)
    private String app;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "token", nullable = false, length = Integer.MAX_VALUE)
    private String token;

    @ToString.Exclude
    @OneToMany(mappedBy = "session")
    @JsonIgnore
    private Set<Request> requests = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "session")
    @JsonIgnore
    private Set<Response> responses = new LinkedHashSet<>();

    public @NotNull NIP46.Session toNip46Session() {
        var result = new NIP46.Session();
        result.setSessionId(getSessionId());
        result.setApp(getApp());
        result.setAccount(getAccount());
        result.setCreatedAt(getCreatedAt());
        result.setToken(getToken());
        result.setSessionId(getSessionId());
        return result;
    }

    public NIP46.Session toNIP46Session() {
        var result = new NIP46.Session();
        result.setSessionId(this.sessionId);
        result.setToken(this.token);
        result.setStatus(this.status);
        result.setApp(this.app);
        result.setAccount(this.account);
        requests.stream().map(Request::toNIP46Request).forEach(result::addRequest);
        responses.stream().map(Response::toNIP46Response).forEach(result::addResponse);
        result.setCreatedAt(this.createdAt);
        return result;
    }

    public static Session fromNIP46Session(NIP46.Session nip46Session) {
        var result = new Session();
        result.setSessionId(nip46Session.getSessionId());
        result.setToken(nip46Session.getToken());
        result.setStatus(nip46Session.getStatus());
        result.setApp(nip46Session.getApp());
        result.setAccount(nip46Session.getAccount());
        nip46Session.getRequests().stream().map(Request::fromNIP46Request).forEach(result::addRequest);
        nip46Session.getResponses().stream().map(Response::fromNIP46Response).forEach(result::addResponse);
        result.setCreatedAt(nip46Session.getCreatedAt());
        return result;
    }

    private void addResponse(@NotNull Response response) {
        responses.add(response);
    }

    private void addRequest(@NotNull Request request) {
        requests.add(request);
    }
}