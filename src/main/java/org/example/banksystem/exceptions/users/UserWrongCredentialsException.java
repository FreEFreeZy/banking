package org.example.banksystem.exceptions.users;

/**
 * Исключение, выбрасываемое при неверных учетных данных пользователя
 * <p>
 * Используется когда предоставленные данные аутентификации (имя пользователя, пароль)
 * не проходят проверку или не соответствуют сохраненным в системе.
 * </p>
 */
public class UserWrongCredentialsException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке
     *
     * @param message детальное сообщение о неверных учетных данных
     */
    public UserWrongCredentialsException(String message) {
        super(message);
    }
}