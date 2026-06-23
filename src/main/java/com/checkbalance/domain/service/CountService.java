package com.checkbalance.domain.service;

import com.checkbalance.domain.entity.Count;
import com.checkbalance.domain.exception.AccountNotFoundException;
import com.checkbalance.domain.repository.CountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CountService {

    private final CountRepository countRepository;

    @Transactional
    public Count findByIdAccount(String id) {
        return countRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
