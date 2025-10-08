package com.project.financeapi.repository;

import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, String> {

    public List<Installment> findByInvoice(Invoice invoice);
}
