/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package nostr.si4n6r.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.si4n6r.core.impl.Principal;
import nostr.si4n6r.util.EncryptionUtil;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author eric
 */
@Log
@WebServlet(name = "ClientAuthServlet", urlPatterns = {"/login"})
public class ClientAuthServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set the response content type to indicate JSON
        response.setContentType("application/json");

        // Retrieve the parameters from the request
        var npub = request.getParameter("npub");
        var password = request.getParameter("password");

        var objectMapper = new ObjectMapper();

        var authResult = new AuthResult();
        authResult.setNpub(npub);
        authResult.setHashedPassword(EncryptionUtil.hashSHA256(password));

        var principal = Principal.getInstance(new PublicKey(npub), password);
        try {
            principal.decryptNsec();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to decrypt the private key with the given password", ex);

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            authResult.setError(ex.getMessage());
            authResult.setResult(AuthResult.RESULT_ERROR);

            var result = objectMapper.writeValueAsString(authResult);
            response.getWriter().write(result);

            return;
        }

        authResult.setResult(AuthResult.RESULT_SUCCESS);

        // Write the JSON object to the response output stream
        response.setStatus(HttpServletResponse.SC_OK);
        var result = objectMapper.writeValueAsString(authResult);
        response.getWriter().write(result);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    @Data
    @NoArgsConstructor
    static class AuthResult {

        static final String RESULT_ERROR = "ERROR";
        static final String RESULT_SUCCESS = "SUCCESS";

        private String npub;
        private String hashedPassword;
        private String result;
        private String error;
    }
}
