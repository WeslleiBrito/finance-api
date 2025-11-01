package com.project.financeapi.repository;

import com.project.financeapi.entity.account.InvestmentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentAccountRepository extends JpaRepository<InvestmentAccount, String> {
}
