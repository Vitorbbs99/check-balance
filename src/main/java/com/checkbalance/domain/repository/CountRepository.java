package com.checkbalance.domain.repository;

import com.checkbalance.domain.entity.Count;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountRepository extends JpaRepository<Count, String> {
}
