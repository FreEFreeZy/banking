package org.example.banksystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CardResponse {
    private Integer card_id;
    private String cardMask;
    private String cardHolder;
    private Date cardExp;
    private String status;
}
