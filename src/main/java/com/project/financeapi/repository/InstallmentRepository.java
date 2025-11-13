package com.project.financeapi.repository;

import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstallmentRepository extends JpaRepository<Installment, UUID> {

    public List<Installment> findByInvoice(Invoice invoice);

    @Query("""
    SELECT i FROM Installment i
    WHERE i.id = :id AND i.createdBy.id = :userId
""")
    Optional<Installment> findCreditCardByCreatedByAndId(UUID userId, UUID id);

}
