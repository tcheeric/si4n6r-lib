package nostr.si4n6r.storage.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountProxy extends BaseActorProxy {
    @NonNull
    private String privateKey;
    @NonNull
    private ApplicationProxy application;
}
