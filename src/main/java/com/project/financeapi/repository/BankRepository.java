package com.project.financeapi.repository;

import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {

    @Query("""
        SELECT b FROM Bank b
        WHERE b.id = :id
          AND (b.createdBy IS NULL OR b.createdBy = :user)
    """)
    Optional<Bank> findByCreatedByAndId(User user, UUID id);

    @Query("""
        SELECT b FROM Bank b
        WHERE b.createdBy IS NULL OR b.createdBy = :user
    """)
    List<Bank> findAllByCreatedBy(User user);
}
