package com.checkbalance.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventDTO {

    private String id;
    private String type;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Long timestamp;
}
