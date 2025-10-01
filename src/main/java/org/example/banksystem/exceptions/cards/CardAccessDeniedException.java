package org.example.banksystem.exceptions.cards;

/**
 * Исключение, выбрасываемое при попытке доступа к карте без необходимых прав
 * <p>
 * Используется когда пользователь пытается выполнить операцию с картой,
 * которая ему не принадлежит или к которой у него нет доступа.
 * </p>
 */
public class CardAccessDeniedException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке
     *
     * @param message детальное сообщение об ошибке доступа
     */
    public CardAccessDeniedException(String message) {
        super(message);
    }
}