package com.project.financeapi.repository;

import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enumSystem.InstrumentNature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentInstrumentRepository extends JpaRepository<PaymentInstrumentBase, UUID> {


    @Query("""
                SELECT p
                FROM PaymentInstrumentBase p
                WHERE (p.createdBy.id = :userId OR p.createdBy IS NULL)
            """)
    List<PaymentInstrumentBase> findByCreatedAll(
            @Param("userId") UUID userId
    );


    @Query("""
                SELECT p
                FROM PaymentInstrumentBase p
                WHERE p.id = :id
                  AND (p.createdBy.id = :userId or p.createdBy is null)
            """)
    Optional<PaymentInstrumentBase> findByIdAndCreatedBy(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("""
                SELECT p
                FROM PaymentInstrumentBase p
                WHERE LOWER(p.name) = LOWER(:name) AND (p.createdBy.id = :userId or p.createdBy is null)
            """)
    Optional<PaymentInstrumentBase> findByName(@Param("userId") UUID userId, @Param("name") String name);

    @Query("""
                SELECT p
                FROM PaymentInstrumentBase p
                WHERE p.id = :id
                  AND (p.createdBy.id = :userId or p.createdBy is null)
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
                  AND (p.createdBy.id = :userId or p.createdBy is null)
            """)
    Optional<PaymentInstrumentBase> findByIdAndUser(
            UUID id,
            UUID userId
    );
}
