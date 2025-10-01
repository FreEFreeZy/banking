package org.example.banksystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Сущность банковской карты
 * <p>
 * Представляет банковскую карту в системе. Содержит основную информацию о карте,
 * включая зашифрованный номер, данные владельца, срок действия, статус и баланс.
 * Карта связана с пользователем через поле cardholder (имя пользователя).
 * </p>
 *
 * @author George
 * @version 1.0
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "cards")
public class Card {

    /**
     * Уникальный идентификатор карты
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cardId;

    /**
     * Зашифрованный номер банковской карты
     */
    @Column(nullable = false)
    private String encryptedCardNumber;

    /**
     * Имя владельца карты (ссылка на пользователя)
     */
    @Column(nullable = false)
    private String cardholder;

    /**
     * Срок действия карты
     */
    @Column(nullable = false)
    private Date expiry_date;

    /**
     * Текущий статус карты (активна, заблокирована, просрочена)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    /**
     * Текущий баланс карты
     */
    @Column(nullable = false)
    private Double balance;

    /**
     * Конструктор для создания новой карты без указания идентификатора
     *
     * @param encryptedCardNumber зашифрованный номер карты
     * @param cardholder имя владельца карты
     * @param expiry_date срок действия карты
     * @param status начальный статус карты
     * @param balance начальный баланс карты
     */
    public Card(String encryptedCardNumber, String cardholder, Date expiry_date, CardStatus status, Double balance) {
        this.encryptedCardNumber = encryptedCardNumber;
        this.cardholder = cardholder;
        this.expiry_date = expiry_date;
        this.status = status;
        this.balance = balance;
    }
}