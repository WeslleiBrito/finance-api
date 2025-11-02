package com.project.financeapi.repository;

import com.project.financeapi.entity.account.WalletAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, UUID> {
}
