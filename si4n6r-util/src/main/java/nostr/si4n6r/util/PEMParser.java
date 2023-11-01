package nostr.si4n6r.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Data
@Log
public class PEMParser {

    private final PEMWithHeaders pemWithHeaders;

    public PEMParser(@NonNull String pem) {
        this.pemWithHeaders = parsePEMWithHeaders(pem);
    }

    public byte[] getPrivateKey() {
        log.log(Level.INFO, "privateKeyFileBytes: {0}", pemWithHeaders.getPrivateKey());
        return Base64.getDecoder().decode(pemWithHeaders.getPrivateKey());
    }

    public String getProcType() {
        return pemWithHeaders.getHeaders().get("Proc-Type");
    }

    public String getDekInfo() {
        return pemWithHeaders.getHeaders().get("DEK-Info");
    }

    public byte[] getSalt() {
        var salt = getDekInfo().split(",")[1];
        log.log(Level.INFO, "Salt: {0}", salt);
        return Base64.getDecoder().decode(salt);
    }

    @Data
    @AllArgsConstructor
    static class PEMWithHeaders {
        private Map<String, String> headers;
        private String privateKey;
    }

    // Write a class that can parse PEM files and extract the private key and the headers.
    // The PEM file format is described here: https://en.wikipedia.org/wiki/Privacy-Enhanced_Mail
    private PEMWithHeaders parsePEMWithHeaders(String pem) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = pem.split("\n");
        StringBuilder privateKey = new StringBuilder();
        boolean isPrivateKey = false;
        for (String line : lines) {
            if (line.startsWith("-----BEGIN")) {
            } else if (line.startsWith("-----END")) {
                isPrivateKey = false;
            } else if (isPrivateKey) {
                privateKey.append(line);
            } else if (line.contains(":")) {
                String[] header = line.split(":");
                headers.put(header[0].trim(), header[1].trim());
            } else {
                isPrivateKey = true;
                privateKey.append(line);
            }
        }
        return new PEMWithHeaders(headers, privateKey.toString());
    }
}
