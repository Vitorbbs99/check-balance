package com.checkbalance.domain.service;

import com.checkbalance.domain.entity.Count;
import com.checkbalance.domain.exception.AccountNotFoundException;
import com.checkbalance.domain.repository.CountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountServiceTest {

    @Mock
    private CountRepository countRepository;

    @InjectMocks
    private CountService countService;

    @Test
    @DisplayName("Should return Count when account exists in database")
    void shouldReturnCountWhenAccountExists() {
        // Arrange
        String countId = "5b19c8b6-0cc4-4c72-a989-0c2ee15fa975";

        Count mockCount = new Count();
        mockCount.setId(countId);
        mockCount.setOwner("315e3cfe-f4af-4cd2-b298-a449e614349a");
        mockCount.setStatus("ENABLED");
        mockCount.setAmount(new BigDecimal("183.12"));
        mockCount.setCurrency("BRL");
        mockCount.setUpdatedAt(Instant.now());

        // Simula o comportamento do Repository
        when(countRepository.findById(countId)).thenReturn(Optional.of(mockCount));

        // Act (Execução do método)
        Count result = countService.findByIdAccount(countId);

        // Verificações se o resultado é o esperado
        assertNotNull(result, "The returned count should not be null");
        assertEquals(countId, result.getId(), "The ID should match the requested one");
        assertEquals(new BigDecimal("183.12"), result.getAmount(), "The amount should match");

        // Garante que o método do repositório foi chamado exatamente uma vez
        verify(countRepository, times(1)).findById(countId);
    }

    @Test
    @DisplayName("Should return empty Optional when account does not exist in database")
    void shouldReturnEmptyWhenAccountDoesNotExist() {
        // Arrange
        String countId = "non-existent-id";

        // Simulando que o repositório não achou nada
        when(countRepository.findById(countId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> {
            countService.findByIdAccount(countId);
        }, "Should throw AccountNotFoundException");

        verify(countRepository, times(1)).findById(countId);
    }
}