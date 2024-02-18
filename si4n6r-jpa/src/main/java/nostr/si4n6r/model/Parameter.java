package nostr.si4n6r.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.api.NIP46;

@Getter
@Setter
@Entity(name = "Parameter")
@Table(name = "t_parameter")
public class Parameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @NotNull
    @Column(name = "value", nullable = false, length = Integer.MAX_VALUE)
    private String value;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    public NIP46.Parameter toNIP46Parameter() {
        var result = new NIP46.Parameter();
        result.setName(this.name);
        result.setValue(this.value);
        result.setRequest(this.request.toNIP46Request());
        return result;
    }

    public static Parameter fromNIP46Parameter(@NonNull NIP46.Parameter parameter) {
        var result = new Parameter();
        result.setName(parameter.getName());
        result.setValue(parameter.getValue());
        result.setRequest(Request.fromNIP46Request(parameter.getRequest()));
        return result;
    }
}