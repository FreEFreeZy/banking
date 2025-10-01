package org.example.banksystem.exceptions.cards;

/**
 * Исключение, выбрасываемое при неверных учетных данных карты
 * <p>
 * Используется когда предоставленные данные карты (номер, срок действия, CVV и т.д.)
 * не проходят проверку или не соответствуют ожидаемым значениям.
 * </p>
 */
public class CardWrongCredentials extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке
     *
     * @param message детальное сообщение о неверных учетных данных карты
     */
    public CardWrongCredentials(String message) {
        super(message);
    }
}