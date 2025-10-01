package org.example.banksystem.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.banksystem.dto.response.UserResponse;
import org.example.banksystem.entity.Role;
import org.example.banksystem.entity.User;
import org.example.banksystem.exceptions.users.UserNotFoundException;
import org.example.banksystem.exceptions.users.UserWrongCredentialsException;
import org.example.banksystem.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Сервис для операций с пользователями системы
 * <p>
 * Предоставляет бизнес-логику для управления пользователями,
 * включая создание, обновление, удаление и получение информации о пользователях.
 * </p>
 *
 * @author George
 * @version 1.0
 */
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * Преобразует сущность User в DTO UserResponse
     *
     * @param user сущность пользователя для преобразования
     * @return DTO с данными пользователя для ответа API
     */
    public UserResponse parseUser(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getPassword(),
                user.getRole().name()
        );
    }

    /**
     * Получает всех пользователей системы
     *
     * @return список DTO со всеми пользователями
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::parseUser).toList();
    }

    /**
     * Создает нового пользователя в системе
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @param role роль пользователя в системе
     * @throws UserWrongCredentialsException если пользователь уже существует или роль невалидна
     */
    @Transactional
    public void addUser(String username, String password, String role) {
        if (userRepository.existsById(username)) {
            throw new UserWrongCredentialsException("User already exists");
        }
        if (Arrays.stream(Role.values()).filter(cardStatus -> cardStatus.name().equals(role)).findFirst().isEmpty()) {
            throw new UserWrongCredentialsException("Role not found");
        }
        userRepository.save(new User(username, bCryptPasswordEncoder.encode(password), Role.valueOf(role)));
    }

    /**
     * Обновляет данные существующего пользователя
     *
     * @param username имя пользователя для обновления
     * @param password новый пароль пользователя
     * @param role новая роль пользователя
     * @throws UserNotFoundException если пользователь не найден
     * @throws UserWrongCredentialsException если роль невалидна
     */
    @Transactional
    public void updateUser(String username, String password, String role) {
        if (!userRepository.existsById(username)) {
            throw new UserNotFoundException("User not found");
        }
        if (Arrays.stream(Role.values()).filter(cardStatus -> cardStatus.name().equals(role)).findFirst().isEmpty()) {
            throw new UserWrongCredentialsException("Role not found");
        }
        userRepository.save(new User(username, bCryptPasswordEncoder.encode(password), Role.valueOf(role)));
    }

    /**
     * Удаляет пользователя по имени
     *
     * @param username имя пользователя для удаления
     * @throws UserNotFoundException если пользователь не найден
     */
    @Transactional
    public void delete(String username){
        if (!userRepository.existsById(username)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(username);
    }
}