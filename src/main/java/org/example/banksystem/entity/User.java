package org.example.banksystem.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Сущность пользователя системы
 * <p>
 * Представляет пользователя банковской системы. Содержит учетные данные пользователя
 * и его роль для управления доступом. Реализует интерфейс UserDetails для интеграции
 * с Spring Security.
 * </p>
 *
 * @author George
 * @version 1.0
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    /**
     * Уникальное имя пользователя, используется как идентификатор
     */
    @Id
    private String username;

    /**
     * Зашифрованный пароль пользователя
     */
    @Column(nullable = false)
    private String password;

    /**
     * Роль пользователя в системе определяющая уровень доступа
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Возвращает список прав доступа пользователя
     *
     * @return коллекция ролей пользователя
     * @see UserDetails#getAuthorities()
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    /**
     * Возвращает пароль пользователя для аутентификации
     *
     * @return зашифрованный пароль
     * @see UserDetails#getPassword()
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Возвращает имя пользователя для аутентификации
     *
     * @return имя пользователя
     * @see UserDetails#getUsername()
     */
    @Override
    public String getUsername() {
        return username;
    }
}