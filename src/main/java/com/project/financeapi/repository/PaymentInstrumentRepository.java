package com.project.financeapi.repository;

import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentInstrumentRepository extends JpaRepository<PaymentInstrumentBase, String> {


    List<PaymentInstrumentBase> findByCreatedBy(User user);

    Optional<PaymentInstrumentBase> findByIdAndCreatedBy(String id, User user);
}
