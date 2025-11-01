package com.project.financeapi.repository;

import com.project.financeapi.entity.account.CheckingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, String> {
}
