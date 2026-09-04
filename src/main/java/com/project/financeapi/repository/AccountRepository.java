package com.project.financeapi.repository;

import com.project.financeapi.entity.User;
import com.project.financeapi.entity.base.AccountBase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountBase, UUID> {

    // Use JOIN FETCH to avoid N+1 queries when fetching accounts and their banks
    @Query(value = "SELECT a FROM AccountBase a LEFT JOIN FETCH a.bank WHERE a.accountHolder = :user",
            countQuery = "SELECT COUNT(a) FROM AccountBase a WHERE a.accountHolder = :user")
    Page<AccountBase> findByAccountHolder(@Param("user") User user, Pageable pageable);

    Optional<AccountBase> findByAccountHolderAndId(User user, UUID id);

    // 🌟 NOVO: Busca a conta e aplica o LOCK PESSIMISTA na linha do banco de dados
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountBase a WHERE a.id = :id")
    Optional<AccountBase> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            SELECT COUNT(a) > 0
            FROM AccountBase a
            WHERE LOWER(a.name) = LOWER(:name) AND (a.accountHolder.id IS NULL OR a.accountHolder.id = :userId)
           """
    )
    boolean nameExitsByAccountHolderId(
            @Param("name") String name,
            @Param("userId") String userId
    );

    @Query("SELECT " +
            "(SELECT COALESCE(SUM(a.initialValue), 0) FROM AccountBase a WHERE a.accountHolder.id = :userId AND a.status = 'ACTIVE') + " +
            "(SELECT COALESCE(SUM(CASE WHEN t.movementDirection = 'INFLOW' THEN (t.amount + t.interest + t.fine - t.discount) ELSE -(t.amount + t.interest + t.fine - t.discount) END), 0) " +
            "FROM Transaction t WHERE t.account.accountHolder.id = :userId AND t.account.status = 'ACTIVE')")
    BigDecimal sumTotalBalanceByUserId(@Param("userId") String userId);
}