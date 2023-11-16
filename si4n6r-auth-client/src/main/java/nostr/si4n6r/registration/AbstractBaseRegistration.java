/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.si4n6r.registration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.si4n6r.core.impl.BaseActorProxy;

/**
 *
 * @author eric
 * @param <T>
 */
@Log
@Data
@AllArgsConstructor
public abstract class AbstractBaseRegistration<T extends BaseActorProxy> implements Registration<T> {

    private final String actorName;

    // Default constructor
    public AbstractBaseRegistration() {
        this.actorName = null;
    }
}
