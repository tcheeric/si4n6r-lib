package nostr.si4n6r.core;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PublicKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

// TODO - This is not needed anymore, now that we have the session manager. Remove?
@Data
@Log
public class ConnectionManager {

    private final List<Connection> connections;
    private static ConnectionManager instance;

    private ConnectionManager() {
        this.connections = new ArrayList<>();
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public boolean isConnected(@NonNull PublicKey app) {
        var conn = this.connections.stream().filter(c -> c.getPublicKey().equals(app)).findFirst().orElse(null);
        return conn != null;
    }

    public boolean isDisconnected(@NonNull PublicKey app) {
        return !isConnected(app);
    }

    public void connect(@NonNull PublicKey publicKey) {
        log.log(Level.FINER, "connecting {0}...", publicKey);
        if (isDisconnected(publicKey)) {
            this.addConnection(publicKey);
            log.log(Level.INFO, "{0} connected!", publicKey);
        }
    }

    public void disconnect(@NonNull PublicKey publicKey) {
        log.log(Level.FINER, "disconnecting {0}...", publicKey);
        this.removeConnection(publicKey);
    }

    public void addConnection(@NonNull PublicKey publicKey) {
        this.connections.add(new Connection(publicKey));
    }

    public void removeConnection(@NonNull PublicKey publicKey) {
        this.connections.removeIf(c -> c.getPublicKey().equals(publicKey));
    }

    @Data
    public static class Connection {

        private final PublicKey publicKey;
        private Date connectionDate;
        private Date disconnectionDate;
        private Boolean connected;

        public Connection(@NonNull PublicKey publicKey) {
            this.publicKey = publicKey;
            this.connectionDate = new Date();
            this.disconnectionDate = null;
            this.connected = true;
        }
    }

}
