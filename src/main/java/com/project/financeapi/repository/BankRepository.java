package com.project.financeapi.repository;

import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, String> {

    Optional<Bank> findByCreatedByAndId(User user, String id);

    List<Bank> findAllByCreatedBy(User user);
}
