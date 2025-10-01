package org.example.banksystem.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.banksystem.dto.response.CardResponse;
import org.example.banksystem.entity.Card;
import org.example.banksystem.entity.CardStatus;
import org.example.banksystem.exceptions.cards.CardAccessDeniedException;
import org.example.banksystem.exceptions.cards.CardNotFoundException;
import org.example.banksystem.exceptions.cards.CardNotInService;
import org.example.banksystem.exceptions.cards.CardWrongCredentials;
import org.example.banksystem.exceptions.users.UserNotFoundException;
import org.example.banksystem.repository.CardRepository;
import org.example.banksystem.repository.UserRepository;
import org.example.banksystem.security.CommonsCodecHasher;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Сервис для операций с банковскими картами
 * <p>
 * Предоставляет бизнес-логику для управления банковскими картами,
 * включая создание, блокировку, переводы и получение информации о картах.
 * </p>
 *
 * @author George
 * @version 1.0
 */
@RequiredArgsConstructor
@Service
public class CardService {

    private final CommonsCodecHasher coder;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * Преобразует сущность Card в DTO CardResponse с маскированным номером
     *
     * @param card сущность карты для преобразования
     * @return DTO с данными карты для ответа API
     */
    public CardResponse parseCard(Card card) {
        return new CardResponse(
                card.getCardId(),
                "*".repeat(12).concat(coder.decode(card.getEncryptedCardNumber()).substring(12)),
                card.getCardholder(),
                card.getExpiry_date(),
                card.getStatus().name()
        );
    }

    /**
     * Проверяет принадлежность карты пользователю
     *
     * @param username имя пользователя для проверки
     * @param card_id идентификатор карты
     * @return true если карта принадлежит пользователю, иначе false
     */
    public boolean validateOwner(String username, Integer card_id) {
        return cardRepository.existsByCardholderAndCardId(username, card_id);
    }

    /**
     * Блокирует карту пользователя после проверки прав доступа
     *
     * @param card_id идентификатор карты для блокировки
     * @param username имя пользователя, выполняющего операцию
     * @throws CardAccessDeniedException если пользователь не является владельцем карты
     * @throws CardNotFoundException если карта не найдена
     * @throws CardNotInService если карта уже неактивна
     */
    @Transactional
    public void blockCard(Integer card_id, String username) {
        if (!validateOwner(username, card_id)) {
            throw new CardAccessDeniedException("Access denied");
        }
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new CardNotFoundException("Card not found"));
        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new CardNotInService("Card not in active status");
        }
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.blockCard(card_id);
    }

    /**
     * Выполняет перевод средств между картами пользователя
     *
     * @param from идентификатор карты отправителя
     * @param to идентификатор карты получателя
     * @param amount сумма перевода
     * @param username имя пользователя, выполняющего операцию
     * @throws CardAccessDeniedException если пользователь не является владельцем одной из карт
     */
    @Transactional
    public void transfer(Integer from, Integer to, Double amount, String username) {
        if (!validateOwner(username, from) || !validateOwner(username, to)) {
            throw new CardAccessDeniedException("Access denied");
        }
        cardRepository.decreaseCardBalance(from, amount);
        cardRepository.increaseCardBalance(to, amount);
    }

    /**
     * Получает все карты пользователя по его имени
     *
     * @param username имя пользователя
     * @return список DTO с данными карт пользователя
     */
    public List<CardResponse> getCardsByUsername(String username) {
        return cardRepository.findCardsByUsername(username).map(cards -> cards.stream().map(this::parseCard).toList()).orElse(List.of());
    }

    /**
     * Получает все карты в системе
     *
     * @return список DTO со всеми картами
     */
    public List<CardResponse> getAllCards() {
        return cardRepository.findAll().stream().map(this::parseCard).toList();
    }

    /**
     * Создает новую карту для пользователя
     *
     * @param cardNumber номер карты
     * @param cardholder имя владельца карты
     * @param expiry_date срок действия карты
     * @throws UserNotFoundException если пользователь не найден
     * @throws CardWrongCredentials если карта с таким номером уже существует
     */
    @Transactional
    public void addCard(String cardNumber, String cardholder, Date expiry_date) {
        if (!userRepository.existsById(cardholder)) {
            throw new UserNotFoundException("User not found");
        }
        if (cardRepository.existsByEncryptedCardNumber(coder.decode(cardNumber))) {
            throw new CardWrongCredentials("Card number already taken");
        }
        cardRepository.save(new Card(coder.encode(cardNumber), cardholder, expiry_date, CardStatus.ACTIVE, 0.0));
    }

    /**
     * Обновляет данные существующей карты
     *
     * @param cardId идентификатор карты
     * @param cardNumber номер карты
     * @param cardholder имя владельца
     * @param expiry_date срок действия
     * @param status новый статус карты
     * @param amount новый баланс
     * @throws UserNotFoundException если пользователь не найден
     * @throws CardNotFoundException если карта не найдена
     * @throws CardWrongCredentials если статус невалиден
     */
    @Transactional
    public void updateCard(Integer cardId, String cardNumber, String cardholder, Date expiry_date, String status, Double amount) {
        if (!userRepository.existsById(cardholder)) {
            throw new UserNotFoundException("User not found");
        }
        if (!cardRepository.existsByEncryptedCardNumber(coder.decode(cardNumber))) {
            throw new CardNotFoundException("Card not found");
        }
        if (Arrays.stream(CardStatus.values()).filter(cardStatus -> cardStatus.name().equals(status)).findFirst().isEmpty()) {
            throw new CardWrongCredentials("Status not found");
        }
        cardRepository.save(new Card(cardId, coder.encode(cardNumber), cardholder, expiry_date, CardStatus.valueOf(status), amount));
    }

    /**
     * Удаляет карту по идентификатору
     *
     * @param id идентификатор карты для удаления
     * @throws CardNotFoundException если карта не найдена
     */
    @Transactional
    public void delete(Integer id){
        if (!cardRepository.existsById(id)){
            throw new CardNotFoundException("Card not found");
        }
        cardRepository.deleteById(id);
    }
}