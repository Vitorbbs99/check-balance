package com.checkbalance.domain.service;

import com.checkbalance.domain.entity.Count;
import com.checkbalance.domain.entity.Transaction;
import com.checkbalance.domain.repository.CountRepository;
import com.checkbalance.domain.repository.TransactionRepository;
import com.checkbalance.infrastructure.dto.TransactionMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final CountRepository countRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void processIngestion(TransactionMessageDTO message) {
        var accountInput = message.getAccount();
        var transactionInput = message.getTransaction();

        // Atualiza ou cria a conta - Garante o saldo atualizado
        Count count = countRepository.findById(accountInput.getId())
                .map(existingCount -> {
                    existingCount.setAmount(accountInput.getBalance().getAmount());
                    existingCount.setStatus(accountInput.getStatus());
                    existingCount.setUpdatedAt(Instant.now());
                    return countRepository.save(existingCount);
                })
                .orElseGet(() -> {
                    Count newCount = Count.builder()
                            .id(accountInput.getId())
                            .owner(accountInput.getOwner())
                            .amount(accountInput.getBalance().getAmount())
                            .currency(accountInput.getBalance().getCurrency())
                            .status(accountInput.getStatus())
                            .updatedAt(Instant.now())
                            .build();
                    return countRepository.save(newCount);
                });

        // Salva a transação se ela ainda não foi salva
        if (!transactionRepository.existsById(transactionInput.getId())) {
            Transaction transaction = Transaction.builder()
                    .id(transactionInput.getId())
                    .type(transactionInput.getType())
                    .amount(transactionInput.getAmount())
                    .currency(transactionInput.getCurrency())
                    .status(transactionInput.getStatus())
                    .timestamp(Instant.ofEpochMilli(transactionInput.getTimestamp() / 1000))
                    .count(count)
                    .build();

            transactionRepository.save(transaction);
        }
    }
}
