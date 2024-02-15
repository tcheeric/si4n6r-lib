package nostr.si4n6r.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import nostr.api.NIP46;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity(name = "Method")
@Table(name = "t_method")
public class Method {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = Integer.MAX_VALUE)
    private String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @OneToMany(mappedBy = "method")
    @ToString.Exclude
    @JsonIgnore
    private Set<Request> requests = new LinkedHashSet<>();

    @OneToMany(mappedBy = "method")
    @ToString.Exclude
    @JsonIgnore
    private Set<Response> responses = new LinkedHashSet<>();

    public NIP46.Method toNIP46Method() {
        var result = new NIP46.Method();
        result.setName(this.name);
        requests.stream().map(Request::toNIP46Request).forEach(result::addRequest);
        responses.stream().map(Response::toNIP46Response).forEach(result::addResponse);
        return result;
    }

    public static Method fromNIP46Method(@NonNull NIP46.Method nip46Method) {
        var result = new Method();
        result.setName(nip46Method.getName());
        nip46Method.getRequests().stream().map(Request::fromNIP46Request).forEach(result::addRequest);
        nip46Method.getResponses().stream().map(Response::fromNIP46Response).forEach(result::addResponse);
        return result;
    }

    private void addRequest(@NonNull Request request) {
        requests.add(request);
    }

    private void addResponse(@NonNull Response response) {
        responses.add(response);
    }
}