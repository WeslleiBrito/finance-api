package com.project.financeapi.repository;

import com.project.financeapi.entity.WalletAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, String> {
}
