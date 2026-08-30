package com.project.financeapi.repository;

import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;
import com.project.financeapi.enumSystem.BankStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {

    @NotNull
    @Query("""
        SELECT b FROM Bank b
        WHERE b.id = :id
    """)
    Optional<Bank> findById(@NotNull UUID id);

    @NotNull
    @Query("""
        SELECT b FROM Bank b
    """)
    List<Bank> findAll();

    @Query(
            """
                SELECT COUNT(b) > 0
                FROM Bank b
                WHERE LOWER(b.name) = LOWER(:name)
            """
    )
    boolean nameExitsById(
            @Param("name") String name
    );

    @Query("""
            SELECT b FROM Bank b
            WHERE LOWER(b.name) = LOWER(:name)
    """)
    Optional<Bank> findByName(
            @Param("name") String name
    );

    @Query(
            """
                SELECT b FROM Bank b
                WHERE b.status = :bankStatus
            """
    )
    List<Bank> findAllBankStatus(
            @Param("bankStatus") BankStatus bankStatus
    );
}
