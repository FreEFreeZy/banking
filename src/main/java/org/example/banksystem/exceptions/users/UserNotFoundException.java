package org.example.banksystem.exceptions.users;

/**
 * Исключение, выбрасываемое когда запрашиваемый пользователь не найден в системе
 * <p>
 * Используется при операциях с пользователями, когда пользователь с указанным именем
 * или идентификатором не существует в базе данных.
 * </p>
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке
     *
     * @param message детальное сообщение об ошибке поиска пользователя
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}