package com.project.financeapi.repository;

import com.project.financeapi.entity.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, String> {
}
