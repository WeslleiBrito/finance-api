package com.project.financeapi.repository;

import com.project.financeapi.entity.account.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, UUID> {
}
