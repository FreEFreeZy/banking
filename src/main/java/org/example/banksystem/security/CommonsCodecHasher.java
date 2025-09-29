package org.example.banksystem.security;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class CommonsCodecHasher {

    @Value("${jwt.key}")
    private byte[] key;

    public String encode(String input) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            String base64 = Base64.encodeBase64String(encrypted);
            return base64.substring(0, Math.min(32, base64.length()));

        } catch (Exception e) {
            throw new RuntimeException("Encoding error", e);
        }
    }

    public String decode(String hash) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // Восстанавливаем padding для Base64 если нужно
            String paddedHash = hash;
            while (paddedHash.length() % 4 != 0) {
                paddedHash += "=";
            }

            byte[] decoded = Base64.decodeBase64(paddedHash);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decoding error", e);
        }
    }
}
