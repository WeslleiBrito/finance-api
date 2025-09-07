package com.project.financeapi.repository;

import com.project.financeapi.entity.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, String> {
}
