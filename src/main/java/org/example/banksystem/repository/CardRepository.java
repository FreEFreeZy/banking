package org.example.banksystem.repository;

import org.example.banksystem.entity.Card;
import org.example.banksystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Card в базе данных
 * <p>
 * Предоставляет методы для выполнения операций с банковскими картами,
 * включая поиск, обновление баланса и блокировку карт.
 * Наследует стандартные CRUD операции от JpaRepository.
 * </p>
 *
 * @author George
 * @version 1.0
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {

    /**
     * Находит все карты пользователя по его имени
     *
     * @param username имя пользователя-владельца карт
     * @return Optional список карт пользователя или empty если карт нет
     */
    @Query("SELECT c.cardId, c.encryptedCardNumber, c.cardholder, c.expiry_date, c.status " +
            "FROM Card c WHERE c.cardholder = :username")
    Optional<List<Card>> findCardsByUsername(@Param("username") String username);

    /**
     * Проверяет существование карты по зашифрованному номеру
     *
     * @param encrypted_card_number зашифрованный номер карты
     * @return true если карта с таким номером существует, иначе false
     */
    boolean existsByEncryptedCardNumber(String encrypted_card_number);

    /**
     * Проверяет принадлежность карты пользователю
     *
     * @param cardholder имя пользователя-владельца
     * @param cardId идентификатор карты
     * @return true если карта принадлежит пользователю, иначе false
     */
    boolean existsByCardholderAndCardId(String cardholder, Integer cardId);

    /**
     * Блокирует карту по идентификатору
     *
     * @param cardId идентификатор карты для блокировки
     */
    @Modifying
    @Query("UPDATE Card c SET c.status = 'BLOCKED' WHERE c.cardId = :cardId")
    void blockCard(@Param("cardId") Integer cardId);

    /**
     * Уменьшает баланс карты на указанную сумму
     *
     * @param cardId идентификатор карты
     * @param amount сумма для списания
     */
    @Modifying
    @Query("UPDATE Card c SET c.balance = c.balance - :amount WHERE c.cardId = :cardId")
    void decreaseCardBalance(@Param("cardId") Integer cardId, @Param("amount") Double amount);

    /**
     * Увеличивает баланс карты на указанную сумму
     *
     * @param cardId идентификатор карты
     * @param amount сумма для зачисления
     */
    @Modifying
    @Query("UPDATE Card c SET c.balance = c.balance + :amount WHERE c.cardId = :cardId")
    void increaseCardBalance(@Param("cardId") Integer cardId, @Param("amount") Double amount);
}