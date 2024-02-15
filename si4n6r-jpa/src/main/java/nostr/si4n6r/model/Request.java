package nostr.si4n6r.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nostr.api.NIP46;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "Request")
@Table(name = "t_request")
public class Request {

    public Request() {
        this.createdAt = LocalDateTime.now();
        this.requestUuid = UUID.randomUUID().toString();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "initiator", nullable = false, length = Integer.MAX_VALUE)
    private String initiator;

    @NotNull
    @Column(name = "token", nullable = false, length = Integer.MAX_VALUE)
    private String token;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "request_uuid", nullable = false)
    private String requestUuid;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "method_id", nullable = false)
    private Method method;

    @OneToMany(mappedBy = "request")
    @ToString.Exclude
    @JsonIgnore
    private Set<Parameter> parameters = new LinkedHashSet<>();

    public NIP46.Request toNIP46Request() {
        NIP46.Request result = new NIP46.Request();
        result.setInitiator(this.initiator);
        result.setToken(this.token);
        result.setCreatedAt(this.createdAt);
        result.setRequestUuid(this.requestUuid);
        result.setSession(this.session.toNIP46Session());
        parameters.stream().map(Parameter::toNIP46Parameter).forEach(result::addParameter);
        return result;
    }

    public static Request fromNIP46Request(@NotNull NIP46.Request request) {
        Request result = new Request();
        result.setInitiator(request.getInitiator());
        result.setToken(request.getToken());
        result.setCreatedAt(request.getCreatedAt());
        result.setRequestUuid(request.getRequestUuid());
        result.setSession(Session.fromNIP46Session(request.getSession()));
        request.getParameters().stream().map(Parameter::fromNIP46Parameter).forEach(result::addParameter);
        return result;
    }

    private void addParameter(@NotNull Parameter parameter) {
        parameters.add(parameter);
    }
}