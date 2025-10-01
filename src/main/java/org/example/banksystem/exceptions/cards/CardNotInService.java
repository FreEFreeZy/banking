package org.example.banksystem.exceptions.cards;

/**
 * Исключение, выбрасываемое при попытке операции с картой, которая не в сервисе
 * <p>
 * Используется когда карта находится в статусе, не позволяющем выполнять операции,
 * например: заблокирована, неактивна или просрочена.
 * </p>
 */
public class CardNotInService extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением об ошибке
     *
     * @param message детальное сообщение о причине недоступности карты
     */
    public CardNotInService(String message) {
        super(message);
    }
}