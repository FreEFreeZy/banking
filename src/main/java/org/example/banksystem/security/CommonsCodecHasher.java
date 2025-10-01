package org.example.banksystem.security;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Компонент для шифрования и дешифрования данных с использованием AES алгоритма
 * <p>
 * Предоставляет методы для кодирования и декодирования чувствительных данных,
 * таких как номера банковских карт, с использованием симметричного шифрования AES.
 * Использует Base64 для кодирования бинарных данных в строковый формат.
 * </p>
 *
 * @author George
 * @version 1.0
 */
@Component
public class CommonsCodecHasher {

    /**
     * Ключ шифрования, настраиваемый через properties файл
     */
    @Value("${codec.key}")
    private byte[] key;

    /**
     * Шифрует входную строку с использованием AES алгоритма
     *
     * @param input исходная строка для шифрования
     * @return зашифрованная строка в Base64 формате (обрезанная до 32 символов)
     * @throws RuntimeException если происходит ошибка шифрования
     */
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

    /**
     * Дешифрует зашифрованную строку обратно в исходный текст
     *
     * @param hash зашифрованная строка в Base64 формате
     * @return расшифрованная исходная строка
     * @throws RuntimeException если происходит ошибка дешифрования
     */
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