package nostr.si4n6r.core.impl.methods;

import nostr.api.NIP01;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.si4n6r.core.IMethod;
import nostr.si4n6r.signer.methods.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("nip-46 methods")
public class MethodTest {

    @Test
    @DisplayName("Test connect method")
    public void connect() {
        var connect = new Connect(new PublicKey("9cb64796ed2c5f18846082cae60c3a18d7a506702cdff0276f86a2ea68a94123"));

        assertEquals("connect", connect.getName());
        assertEquals(1, connect.getParams().size());
        assertEquals("9cb64796ed2c5f18846082cae60c3a18d7a506702cdff0276f86a2ea68a94123", connect.getParams().get(0).get().toString());
    }

    @Test
    @DisplayName("Test describe method")
    public void describe() {
        var describe = new Describe();

        assertEquals(IMethod.Constants.METHOD_DESCRIBE, describe.getName());
        assertEquals(0, describe.getParams().size());
    }

    @Test
    @DisplayName("Test disconnect method")
    public void disconnect() {
        var disconnect = new Disconnect(new PublicKey("9cb64796ed2c5f18846082cae60c3a18d7a506702cdff0276f86a2ea68a94123"));

        assertEquals("disconnect", disconnect.getName());
        assertEquals(1, disconnect.getParams().size());
        assertEquals("9cb64796ed2c5f18846082cae60c3a18d7a506702cdff0276f86a2ea68a94123", disconnect.getParams().get(0).get().toString());
    }

    @Test
    @DisplayName("Test get public key method")
    public void getPublicKey() {
        var gpk = new GetPublicKey();

        assertEquals(IMethod.Constants.METHOD_GET_PUBLIC_KEY, gpk.getName());
        assertEquals(0, gpk.getParams().size());
    }

    @Test
    @DisplayName("Test sign event method")
    public void signEvent() {
        var event = NIP01.createTextNoteEvent("test");
        var signEvent = new SignEvent(event);

        assertEquals(IMethod.Constants.METHOD_SIGN_EVENT, signEvent.getName());
        assertEquals(1, signEvent.getParams().size());

        GenericEvent _event = (GenericEvent) signEvent.getParams().get(0).get();
        assertEquals("test", _event.getContent());
    }
}
