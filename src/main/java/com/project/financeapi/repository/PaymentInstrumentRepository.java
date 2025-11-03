package com.project.financeapi.repository;

import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enums.InstrumentNature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentInstrumentRepository extends JpaRepository<PaymentInstrumentBase, UUID> {


    List<PaymentInstrumentBase> findByCreatedBy(User user);

    Optional<PaymentInstrumentBase> findByIdAndCreatedBy(UUID id, User user);

    @Query("""
        SELECT p
        FROM PaymentInstrumentBase p
        WHERE p.id = :id
          AND p.createdBy.id = :userId
          AND p.instrumentNature = :nature
    """)
    Optional<PaymentInstrumentBase> findByIdAndUserAndNature(
            UUID id,
            UUID userId,
            InstrumentNature nature
    );

    @Query("""
        SELECT p
        FROM PaymentInstrumentBase p
        WHERE p.id = :id
          AND p.createdBy.id = :userId
    """)
    Optional<PaymentInstrumentBase> findByIdAndUser(
            UUID id,
            UUID userId
    );
}
