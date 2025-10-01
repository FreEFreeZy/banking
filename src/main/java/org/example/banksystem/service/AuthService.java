package org.example.banksystem.service;

import lombok.RequiredArgsConstructor;
import org.example.banksystem.entity.Role;
import org.example.banksystem.entity.User;
import org.example.banksystem.exceptions.users.UserNotFoundException;
import org.example.banksystem.exceptions.users.UserWrongCredentialsException;
import org.example.banksystem.repository.UserRepository;
import org.example.banksystem.security.JwtTokenProvider;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервис аутентификации и авторизации пользователей
 * <p>
 * Предоставляет функциональность для входа в систему, регистрации новых пользователей
 * и загрузки данных пользователя для Spring Security.
 * </p>
 *
 * @author George
 * @version 1.0
 */
@RequiredArgsConstructor
@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Загружает пользователя по имени для Spring Security
     *
     * @param username имя пользователя для поиска
     * @return UserDetails объект пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Выполняет аутентификацию пользователя и создает JWT токен
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return ResponseCookie с JWT токеном для аутентификации
     * @throws UserNotFoundException если пользователь не найден
     * @throws UserWrongCredentialsException если пароль неверный
     */
    public ResponseCookie login(String username, String password) {
        User user = userRepository.findById(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserWrongCredentialsException("Wrong password");
        }
        return ResponseCookie.from("Authorization", jwtTokenProvider.createToken(user.getUsername(), user.getAuthorities()))
                .path("/")
                .secure(false)
                .maxAge(3600)
                .httpOnly(true)
                .sameSite("Strict")
                .build();
    }

    /**
     * Регистрирует нового пользователя в системе
     *
     * @param username имя нового пользователя
     * @param password пароль нового пользователя
     * @throws UserWrongCredentialsException если пользователь с таким именем уже существует
     */
    public void registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserWrongCredentialsException("User already exists");
        }
        User user = new User(username, passwordEncoder.encode(password), Role.ROLE_USER);
        userRepository.save(user);
    }
}