/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package nostr.si4n6r.registration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.si4n6r.core.impl.AccountProxy;
import nostr.si4n6r.core.impl.BaseActorProxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author eric
 */
@Data
@Log
@EqualsAndHashCode(callSuper = true)
public class AccountRegistration extends AbstractBaseRegistration<AccountProxy> {

    private final String password;

    public AccountRegistration(@NonNull String password) {
        super(BaseActorProxy.VAULT_ACTOR_ACCOUNT);
        this.password = password;
    }

    @Override
    public void register(@NonNull AccountProxy proxy) {
        HttpURLConnection connection = null;
        try {
            // Specify the base URL of the servlet
            var baseUrl = "http://localhost:8080/si4n6r-auth-server-1.0-SNAPSHOT/register";

            // Specify parameters (replace with your actual parameter values)
            Map<String, String> parameters = Map.of(
                    "npub", proxy.getPublicKey(),
                    "nsec", proxy.getPrivateKey(),
                    "password", password,
                    "app", proxy.getApplication().getPublicKey()
            );

            // Construct the URL with parameters
            var urlString = buildUrlWithParameters(baseUrl, parameters);

            log.log(Level.INFO, "URL: {0}", urlString);

            var url = new URI(urlString).toURL();

            // Complete the method. Write code to connect the url and return the server output
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();

            // Print the response content
            log.log(Level.INFO, "Response Content: {0}", responseContent.toString());

        } catch (IOException | URISyntaxException e) {
            log.log(Level.SEVERE, "An error has occurred: ", e);
        } finally {
            // Close the connection
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String buildUrlWithParameters(String baseUrl, Map<String, String> parameters) {
        String paramString = parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return baseUrl + "?" + paramString;
    }

}
