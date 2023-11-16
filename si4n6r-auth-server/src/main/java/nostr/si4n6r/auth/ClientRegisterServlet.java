/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package nostr.si4n6r.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.si4n6r.core.impl.AccountProxy;
import nostr.si4n6r.core.impl.ApplicationProxy;
import nostr.si4n6r.core.impl.BaseActorProxy;
import nostr.si4n6r.core.impl.Principal;
import nostr.si4n6r.storage.Vault;
import nostr.si4n6r.util.EncryptionUtil;

import java.io.IOException;
import java.util.ServiceLoader;
import java.util.logging.Level;

/**
 * @author eric
 */
@WebServlet(name = "ClientRegisterServlet", urlPatterns = {"/register"})
@Log
public class ClientRegisterServlet extends HttpServlet {

    private static Vault getVault(@NonNull String entity) {
        return ServiceLoader
                .load(Vault.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(v -> entity.equals(v.getEntityName()))
                .findFirst()
                .get();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        log.log(Level.INFO, "Processing the servlet request");

        // Retrieve parameters from the request
        var npub = request.getParameter("npub");
        var nsec = request.getParameter("nsec");
        var password = request.getParameter("password");
        var application = request.getParameter("app");

        Principal principal = Principal.getInstance(new PublicKey(npub), password);
        nostr.si4n6r.core.impl.SecurityManager securityManager = nostr.si4n6r.core.impl.SecurityManager.getInstance();
        securityManager.addPrincipal(principal);

        var account = new AccountProxy();
        account.setPrivateKey(nsec);
        account.setPublicKey(npub);
        account.setId(System.currentTimeMillis());
        account.setApplication(new ApplicationProxy(application));

        Vault<AccountProxy> vault = getVault();

        log.log(Level.INFO, "Storing the account in the vault");
        vault.store(account);

        // Set the response content type to indicate JSON
        response.setContentType("application/json");

        var result = new RegisterResult();
        result.setNpub(npub);
        result.setNsec(EncryptionUtil.hashSHA256(nsec));
        result.setPassword(EncryptionUtil.hashSHA256(password));
        result.setResult(ClientAuthServlet.AuthResult.RESULT_SUCCESS);

        log.log(Level.INFO, "Result: {0}", result);

        response.setStatus(HttpServletResponse.SC_OK);

        var objectMapper = new ObjectMapper();
        final String output = objectMapper.writeValueAsString(result);
        log.log(Level.INFO, "output: {0}", output);
        response.getWriter().write(output);
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private Vault<AccountProxy> getVault() {
        return getVault(BaseActorProxy.VAULT_ACTOR_ACCOUNT);
    }

    @Data
    @NoArgsConstructor
    static class RegisterResult {

        private String npub;
        private String nsec;
        private String password;
        private String result;
        private String error;
    }

}
