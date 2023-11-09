package nostr.si4n6r.core.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
// TODO - Rename appropriately to avoid any confusion
public class AccountProxy extends BaseActorProxy {
    private String privateKey;
    private ApplicationProxy application;
}
