package org.example.banksystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис для работы с JWT токенами
 * <p>
 * Предоставляет функциональность для создания, валидации и извлечения данных из JWT токенов.
 * Используется для аутентификации и авторизации пользователей в системе.
 * </p>
 *
 * @author George
 * @version 1.0
 */
@Service
public class JwtTokenProvider {

    /**
     * Секретный ключ для подписи JWT токенов
     */
    @Value("${jwt.key}")
    private String key;

    /**
     * Время жизни токена в миллисекундах
     */
    @Value("${jwt.expiration_time}")
    private long expiration;

    /**
     * Создает секретный ключ для подписи токенов
     *
     * @return SecretKey для подписи JWT токенов
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Создает JWT токен для пользователя
     *
     * @param username имя пользователя
     * @param authorities коллекция прав пользователя
     * @return JWT токен в виде строки
     */
    public String createToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet()));

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Извлекает имя пользователя из JWT токена
     *
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает конкретное утверждение (claim) из токена
     *
     * @param token JWT токен
     * @param claimsResolver функция для извлечения конкретного claim
     * @return значение claim
     * @param <T> тип возвращаемого значения
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлекает все утверждения (claims) из токена
     *
     * @param token JWT токен
     * @return объект Claims со всеми утверждениями
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Проверяет валидность JWT токена
     *
     * @param token JWT токен для проверки
     * @return true если токен валиден, false в противном случае
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}