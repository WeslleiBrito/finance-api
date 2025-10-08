package com.project.financeapi.repository;

import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    List<Invoice> findByCreatedBy(User user);

    Optional<Invoice> findByIdAndCreatedBy(String id, User user);
}
