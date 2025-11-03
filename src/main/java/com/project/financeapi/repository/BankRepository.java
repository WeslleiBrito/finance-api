package com.project.financeapi.repository;

import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {

    Optional<Bank> findByCreatedByAndId(User user, UUID id);

    List<Bank> findAllByCreatedBy(User user);
}
