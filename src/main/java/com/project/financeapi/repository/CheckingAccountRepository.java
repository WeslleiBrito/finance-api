package com.project.financeapi.repository;

import com.project.financeapi.entity.CheckingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, String> {
}
