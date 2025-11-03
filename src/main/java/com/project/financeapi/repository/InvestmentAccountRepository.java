package com.project.financeapi.repository;

import com.project.financeapi.entity.account.InvestmentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvestmentAccountRepository extends JpaRepository<InvestmentAccount, UUID> {
}
