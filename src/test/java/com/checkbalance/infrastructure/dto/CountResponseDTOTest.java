package com.checkbalance.infrastructure.dto;

import com.checkbalance.domain.entity.Count;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CountResponseDTOTest {

    @Test
    @DisplayName("Should map Count entity to CountResponseDTO successfully")
    void shouldMapCountToCountResponseDTOSuccessfully() {
        // Arrange (Preparação dos dados de teste)
        String countId = "5b19c8b6-0cc4-4c72-a989-0c2ee15fa975";
        String ownerId = "315e3cfe-f4af-4cd2-b298-a449e614349a";
        BigDecimal amount = new BigDecimal("183.12");
        String currency = "BRL";
        Instant now = Instant.now();

        Count count = new Count();
        count.setId(countId);
        count.setOwner(ownerId);
        count.setStatus("ENABLED");
        count.setAmount(amount);
        count.setCurrency(currency);
        count.setUpdatedAt(now);

        // Executa
        CountResponseDTO response = CountResponseDTO.create(count);

        // Verificações
        assertNotNull(response, "The response DTO should not be null");
        assertEquals(countId, response.getId());
        assertEquals(ownerId, response.getOwner());
        assertEquals(now, response.getUpdatedAt());

        // Objeto 'balance'
        assertNotNull(response.getBalance(), "The balance object inside DTO should not be null");
        assertEquals(amount, response.getBalance().getAmount());
        assertEquals(currency, response.getBalance().getCurrency());
    }

    @Test
    @DisplayName("Should return null when Count entity is null")
    void shouldReturnNullWhenCountEntityIsNull() {
        // Act
        CountResponseDTO response = CountResponseDTO.create(null);

        // Assert
        assertNull(response, "The response should be null if the input entity is null");
    }
}