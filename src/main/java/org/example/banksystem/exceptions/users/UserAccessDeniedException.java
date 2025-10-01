package org.example.banksystem.exceptions.users;

/**
 * Исключение, выбрасываемое при попытке доступа к ресурсу без необходимых прав пользователя
 * <p>
 * Используется когда пользователь пытается выполнить операцию,
 * для которой у него недостаточно прав или привилегий.
 * </p>
 */
public class UserAccessDeniedException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке
     *
     * @param message детальное сообщение об ошибке доступа
     */
    public UserAccessDeniedException(String message) {
        super(message);
    }
}