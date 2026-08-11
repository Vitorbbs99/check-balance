package com.checkbalance.infrastructure.controller;

import com.checkbalance.domain.entity.Count;
import com.checkbalance.domain.service.CountService;
import com.checkbalance.infrastructure.dto.CountResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/balances")
@RequiredArgsConstructor
public class CountController {

    private final CountService countService;

    @GetMapping("/{id}")
    public ResponseEntity<CountResponseDTO> getBalance(@PathVariable String id) {
        Count count = countService.findByIdAccount(id);
        return ResponseEntity.ok(CountResponseDTO.create(count));
    }
}
