package nostr.si4n6r.util;

import java.nio.charset.StandardCharsets;
import lombok.extern.java.Log;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Log
public class EncryptionUtil {

    public static PublicKey generateAndSavePrivateKey(String privateKeyFile, String password) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        savePrivateKeyAsPEM(keyPair.getPrivate(), privateKeyFile, password);

        return keyPair.getPublic();
    }

    public static PrivateKey loadPrivateKeyFromPEM(String privateKeyFile, String password) throws Exception {
        String pem = Files.readString(Path.of(privateKeyFile));
        byte[] encryptedPrivateKey = extractEncryptedKey(pem);
        byte[] salt = extractSalt(pem);
        SecretKey secretKey = deriveKeyFromPassword(password, salt);
        byte[] privateKeyBytes = decryptPrivateKey(encryptedPrivateKey, secretKey, salt);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    public static byte[] decryptWithPrivateKey(PrivateKey privateKey, byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    public static byte[] encryptWithPublicKey(PublicKey publicKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static String hashSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void savePrivateKeyAsPEM(PrivateKey privateKey, String privateKeyFile, String password) throws Exception {
        byte[] salt = generateSalt();
        byte[] encryptedPrivateKey = encryptPrivateKey(privateKey, password, salt);
        String pem = createPEMFormat(encryptedPrivateKey, salt);
        Files.write(Path.of(privateKeyFile), pem.getBytes(), StandardOpenOption.CREATE);
    }

    private static byte[] extractEncryptedKey(String pem) {
        return new PEMParser(pem).getPrivateKey();
    }

    private static byte[] extractSalt(String pem) {
        return new PEMParser(pem).getSalt();
    }

    private static SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        int iterationCount = 65536;
        int keyLength = 256;
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey tmp = secretKeyFactory.generateSecret(keySpec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private static byte[] generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return salt;
    }

    // write the createPEMFormat () method to create the PEM format that includes the Proc-Type and DEK-Info headers
    private static String createPEMFormat(byte[] encryptedPrivateKey, byte[] salt) {
        String encodedEncryptedKey = Base64.getEncoder().encodeToString(encryptedPrivateKey);
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        return String.format("-----BEGIN ENCRYPTED PRIVATE KEY-----\n"
                + "Proc-Type: 4,ENCRYPTED\n"
                + "DEK-Info: AES-256-CBC,%s\n\n"
                + "%s\n"
                + "-----END ENCRYPTED PRIVATE KEY-----", encodedSalt, encodedEncryptedKey);

    }

    private static byte[] encryptPrivateKey(PrivateKey privateKey, String password, byte[] salt) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey secretKey = deriveKeyFromPassword(password, salt);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(salt));
        return cipher.doFinal(privateKey.getEncoded());
    }

    /*
    private static byte[] encryptPrivateKey(PrivateKey privateKey, String password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, deriveKeyFromPassword(password, generateSalt()));
        return cipher.doFinal(privateKey.getEncoded());
    }
     */
    public static byte[] decryptPrivateKey(byte[] encryptedPrivateKey, SecretKey secretKey, byte[] initializationVector) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(initializationVector));
        return cipher.doFinal(encryptedPrivateKey);
    }
}
