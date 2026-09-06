package com.project.financeapi.repository;

import com.project.financeapi.entity.Transaction;
import com.project.financeapi.enumSystem.MovementDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // 1. PAGINAÇÃO E PREVENÇÃO DE N+1
    // O JOIN FETCH traz a Conta e o Instrumento na mesma viagem ao banco.
    // O countQuery é obrigatório quando usamos JOIN FETCH com paginação no Spring.
    @Query(value = "SELECT t FROM Transaction t " +
            "JOIN FETCH t.account a " +
            "LEFT JOIN FETCH t.paymentInstrument p " + // LEFT JOIN pois pode ser nulo
            "WHERE a.accountHolder.id = :userId",
            countQuery = "SELECT COUNT(t) FROM Transaction t WHERE t.account.accountHolder.id = :userId")
    Page<Transaction> findAllByUserId(@Param("userId") String userId, Pageable pageable);

    boolean existsByReversalOfId(UUID originalTransactionId);

    // 2. PREVENÇÃO DE N+1 NA BUSCA POR ID
    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.account a " +
            "LEFT JOIN FETCH t.paymentInstrument p " +
            "WHERE t.id = :id AND a.accountHolder.id = :userId")
    Optional<Transaction> findByIdAndUserId(@Param("id") UUID id, @Param("userId") String userId);

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.paymentInstrument " +
            "LEFT JOIN FETCH t.installment i " +
            "LEFT JOIN FETCH i.invoice inv " +
            "LEFT JOIN FETCH inv.operationType " +
            "WHERE t.createdBy.id = :userId " +
            "AND t.paymentDate >= :startDate " +
            "AND t.reversed = false " +
            "AND t.movementType != 'REVERSAL'")
    List<Transaction> findTransactionsForDashboard(@Param("userId") String userId, @Param("startDate") LocalDate startDate);

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.paymentInstrument " +
            "LEFT JOIN FETCH t.installment i " +
            "LEFT JOIN FETCH i.invoice inv " +
            "LEFT JOIN FETCH inv.operationType op " + // 🌟 Alias 'op' declarado
            "LEFT JOIN inv.person p " +               // 🌟 LEFT JOIN explícito para aceitar faturas nulas
            "WHERE t.createdBy.id = :userId " +       // 🌟 Busca direta e mais segura
            "AND (:direction IS NULL OR t.movementDirection = :direction) " +
            "AND (:accountId IS NULL OR t.account.id = :accountId) " +
            "AND (CAST(:startDate AS date) IS NULL OR t.paymentDate >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR t.paymentDate <= :endDate) " +
            "AND (:searchName IS NULL " +
            "     OR LOWER(t.observations) LIKE LOWER(CONCAT('%', CAST(:searchName AS string), '%')) " +
            "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:searchName AS string), '%')) " +
            "     OR LOWER(op.name) LIKE LOWER(CONCAT('%', CAST(:searchName AS string), '%'))) " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> searchTransactions(
            @Param("userId") String userId,
            @Param("direction") MovementDirection direction,
            @Param("searchName") String searchName,
            @Param("accountId") UUID accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}