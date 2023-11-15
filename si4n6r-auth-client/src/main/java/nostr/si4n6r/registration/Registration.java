/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package nostr.si4n6r.registration;

import nostr.si4n6r.core.impl.BaseActorProxy;

/**
 *
 * @author eric
 * @param <T>
 */
public interface Registration<T extends BaseActorProxy> {
 
    void register(T actor);
}
