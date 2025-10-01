package org.example.banksystem.exceptions.cards;

/**
 * Исключение, выбрасываемое когда запрашиваемая карта не найдена в системе
 * <p>
 * Используется при операциях с картами, когда карта с указанным идентификатором
 * или номером не существует в базе данных.
 * </p>
 */
public class CardNotFoundException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке
     *
     * @param message детальное сообщение об ошибке поиска карты
     */
    public CardNotFoundException(String message) {
        super(message);
    }
}