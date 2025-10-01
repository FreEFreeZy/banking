package org.example.banksystem.entity;

import org.springframework.security.core.GrantedAuthority;

/**
 * Перечисление ролей пользователей в системе
 * <p>
 * Определяет уровни доступа пользователей в банковской системе.
 * Реализует интерфейс GrantedAuthority для интеграции с Spring Security.
 * </p>
 *
 * @author George
 * @version 1.0
 */
public enum Role implements GrantedAuthority {
    /**
     * Администратор системы - имеет полный доступ ко всем функциям
     */
    ROLE_ADMIN,

    /**
     * Обычный пользователь - имеет доступ к базовым операциям с картами
     */
    ROLE_USER;

    /**
     * Возвращает строковое представление роли для Spring Security
     *
     * @return имя роли в виде строки
     * @see GrantedAuthority#getAuthority()
     */
    @Override
    public String getAuthority() {
        return name();
    }
}