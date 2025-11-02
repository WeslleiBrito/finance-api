package com.project.financeapi.repository;

import com.project.financeapi.entity.account.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {
}
