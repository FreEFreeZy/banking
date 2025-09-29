package org.example.banksystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cardId;

    @Column(nullable = false)
    private String encryptedCardNumber;

    @Column(nullable = false)
    private String cardholder;

    @Column(nullable = false)
    private Date expiry_date;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Double balance;

    public Card(String encryptedCardNumber, String cardholder, Date expiry_date, String status, Double balance) {
        this.encryptedCardNumber = encryptedCardNumber;
        this.cardholder = cardholder;
        this.expiry_date = expiry_date;
        this.status = status;
        this.balance = balance;
    }
}
