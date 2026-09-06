package com.project.financeapi.repository;

import com.project.financeapi.entity.Invoice;
import com.project.financeapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    @Query(value = "SELECT i FROM Invoice i JOIN FETCH i.person JOIN FETCH i.operationType WHERE i.createdBy = :user",
            countQuery = "SELECT COUNT(i) FROM Invoice i WHERE i.createdBy = :user")
    Page<Invoice> findByCreatedBy(@Param("user") User user, Pageable pageable);

    Optional<Invoice> findByIdAndCreatedBy(UUID id, User user);

    List<Invoice> findByPersonId(UUID personId);
}
