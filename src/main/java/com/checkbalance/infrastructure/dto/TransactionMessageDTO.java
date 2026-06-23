package com.checkbalance.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMessageDTO {

    private TransactionEventDTO transaction;
    private AccountEventDTO account;

    public static TransactionMessageDTO create(TransactionEventDTO transaction, AccountEventDTO account) {
        return TransactionMessageDTO.builder()
                .transaction(transaction)
                .account(account)
                .build();
    }
}