package com.project.financeapi.repository;

import com.project.financeapi.entity.InvestmentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentAccountRepository extends JpaRepository<InvestmentAccount, String> {
}
