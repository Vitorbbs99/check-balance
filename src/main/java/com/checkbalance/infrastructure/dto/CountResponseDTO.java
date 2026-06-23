package com.checkbalance.infrastructure.dto;

import com.checkbalance.domain.entity.Count;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountResponseDTO {

    private String id;
    private String owner;
    private BalanceDTO balance;
    private Instant updatedAt;

    public static CountResponseDTO create(Count count) {
        if (count == null) {
            return null;
        }

        BalanceDTO balanceDTO = new BalanceDTO(count.getAmount(), count.getCurrency());

        return new CountResponseDTO(
                count.getId(),
                count.getOwner(),
                balanceDTO,
                count.getUpdatedAt()
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceDTO {
        private BigDecimal amount;
        private String currency;
    }
}