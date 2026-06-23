package com.checkbalance.domain.service;

import com.checkbalance.domain.entity.Count;
import com.checkbalance.domain.entity.Transaction;
import com.checkbalance.domain.repository.CountRepository;
import com.checkbalance.domain.repository.TransactionRepository;
import com.checkbalance.infrastructure.dto.AccountEventDTO;
import com.checkbalance.infrastructure.dto.BalanceEventDTO;
import com.checkbalance.infrastructure.dto.TransactionEventDTO;
import com.checkbalance.infrastructure.dto.TransactionMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private CountRepository countRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private IngestionService ingestionService;

    private TransactionMessageDTO messageMock;
    private String accountId;
    private String transactionId;

    @BeforeEach
    void setUp() {
        accountId = "acc-123";
        transactionId = "tx-999";

        // Montando o cenário padrão
        BalanceEventDTO balance = BalanceEventDTO.builder()
                .amount(new BigDecimal("250.50"))
                .currency("BRL")
                .build();

        AccountEventDTO account = AccountEventDTO.builder()
                .id(accountId)
                .owner("owner-777")
                .status("ENABLED")
                .balance(balance)
                .build();

        TransactionEventDTO transaction = TransactionEventDTO.builder()
                .id(transactionId)
                .type("CREDIT")
                .amount(new BigDecimal("50.00"))
                .currency("BRL")
                .status("APPROVED")
                .timestamp(1751641364590000L) // Exemplo em microssegundos (padrão Go)
                .build();

        messageMock = TransactionMessageDTO.create(transaction, account);
    }

    @Test
    @DisplayName("Should return a new account and save the transaction if the account does not already exist")
    void shouldCreateNewCountAndSaveTransactionWhenCountDoesNotExist() {
        // Arrange
        when(countRepository.findById(accountId)).thenReturn(Optional.empty());
        when(transactionRepository.existsById(transactionId)).thenReturn(false);

        // Mocking os métodos save para retornar o próprio objeto recebido
        when(countRepository.save(any(Count.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ingestionService.processIngestion(messageMock);

        // Assert
        verify(countRepository, times(1)).save(any(Count.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should update the account balance and status if the account already exists.")
    void shouldUpdateExistingCountBalanceAndStatus() {
        // Arrange
        Count existingCount = Count.builder()
                .id(accountId)
                .amount(new BigDecimal("100.00")) // Saldo antigo
                .status("DISABLED")               // Status antigo
                .build();

        when(countRepository.findById(accountId)).thenReturn(Optional.of(existingCount));
        when(transactionRepository.existsById(transactionId)).thenReturn(false);
        when(countRepository.save(any(Count.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ingestionService.processIngestion(messageMock);

        // Assert
        ArgumentCaptor<Count> countCaptor = ArgumentCaptor.forClass(Count.class);
        verify(countRepository).save(countCaptor.capture());

        Count savedCount = countCaptor.getValue();
        assertEquals(new BigDecimal("250.50"), savedCount.getAmount(), "The balance should be updated to the most recent value in the queue.");
        assertEquals("ENABLED", savedCount.getStatus(), "The account status needs to be updated.");
        assertNotNull(savedCount.getUpdatedAt(), "The updatedAt field must be filled in.");
    }

    @Test
    @DisplayName("Corner Case: Do not save the transaction if its ID already exists in the database.")
    void shouldNotSaveTransactionWhenTransactionIdAlreadyExists() {
        // Arrange
        Count existingCount = Count.builder().id(accountId).amount(new BigDecimal("250.50")).build();

        when(countRepository.findById(accountId)).thenReturn(Optional.of(existingCount));
        // Simulando que a transação JÁ existe (Duplicada na fila SQS)
        when(transactionRepository.existsById(transactionId)).thenReturn(true);
        when(countRepository.save(any(Count.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ingestionService.processIngestion(messageMock);

        // Assert
        verify(countRepository, times(1)).save(any(Count.class)); // A conta ainda pode ser atualizada
        verify(transactionRepository, never()).save(any(Transaction.class)); // A transação DEVE ser ignorada
    }

    @Test
    @DisplayName("Corner Case: Must correctly convert the Go timestamp from microseconds to Java Instant.")
    void shouldConvertGoMicrosTimestampToJavaInstantCorrectly() {
        // Arrange
        when(countRepository.findById(accountId)).thenReturn(Optional.empty());
        when(transactionRepository.existsById(transactionId)).thenReturn(false);
        when(countRepository.save(any(Count.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ingestionService.processIngestion(messageMock);

        // Assert
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());

        Transaction savedTx = txCaptor.getValue();

        // 1751641364590000 microssegundos / 1000 = 1751641364590 milissegundos
        // 1751641364590L em milissegundos equivale a: 2025-07-04T15:02:44.590Z
        Instant expectedInstant = Instant.ofEpochMilli(1751641364590L);

        assertEquals(expectedInstant, savedTx.getTimestamp(), "The Go timestamp conversion needs to be identical.");
    }
}
