package com.project.financeapi.repository;

import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.BankStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {

    @Query("""
        SELECT b FROM Bank b
        WHERE b.id = :id
          AND (b.createdBy IS NULL OR b.createdBy = :user)
    """)
    Optional<Bank> findByCreatedByAndId(User user, UUID id);

    @Query("""
        SELECT b FROM Bank b
        WHERE b.createdBy IS NULL OR b.createdBy = :user
    """)
    List<Bank> findAllByCreatedBy(User user);

    @Query(
            """
                SELECT COUNT(b) > 0
                FROM Bank b
                WHERE LOWER(b.name) = LOWER(:name) AND (b.createdBy IS NULL OR b.createdBy = :user)
            """
    )
    boolean nameExitsByCreatedById(
            @Param("user") User user,
            @Param("name") String name
    );

    @Query("""
            SELECT b FROM Bank b
            WHERE LOWER(b.name) = LOWER(:name) AND (b.createdBy IS NULL OR b.createdBy = :user)
    """)
    Optional<Bank> findByCreatedByByName(
            @Param("user") User user,
            @Param("name") String name
    );

    @Query(
            """
                SELECT b FROM Bank b
                WHERE b.status = :bankStatus AND (b.createdBy IS NULL OR b.createdBy = :user)
            """
    )
    List<Bank> findAllByUserBankStatus(
            @Param("user") User user,
            @Param("bankStatus") BankStatus bankStatus
    );
}
