package org.example.banksystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "cards")
public class Card {

    @Id
    private String card_number;

    @Column(nullable = false)
    private Integer user_id;

    @Column(nullable = false)
    private String card_owner;

    @Column(nullable = false)
    private Integer card_cvv;
}
