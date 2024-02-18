package nostr.si4n6r.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.api.NIP46;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "Response")
@Table(name = "t_response")
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "response_uuid", nullable = false)
    private String responseUuid;

    @NotNull
    @Column(name = "result", nullable = false, length = Integer.MAX_VALUE)
    private String result;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "method_id", nullable = false)
    private Method method;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    public NIP46.Response toNIP46Response() {
        var result = new NIP46.Response();
        result.setResponseUuid(this.responseUuid);
        result.setResult(this.result);
        result.setCreatedAt(this.createdAt);
        result.setSession(this.session.toNIP46Session());
        return result;
    }

    public static Response fromNIP46Response(@NonNull NIP46.Response response) {
        var result = new Response();
        result.setResponseUuid(response.getResponseUuid());
        result.setResult(response.getResult());
        result.setCreatedAt(response.getCreatedAt());
        result.setSession(Session.fromNIP46Session(response.getSession()));
        return result;
    }
}