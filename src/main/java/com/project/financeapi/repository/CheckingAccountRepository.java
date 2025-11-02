package com.project.financeapi.repository;

import com.project.financeapi.entity.account.CheckingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, UUID> {
}
