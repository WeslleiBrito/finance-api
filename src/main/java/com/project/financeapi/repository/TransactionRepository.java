package com.project.financeapi.repository;

import com.project.financeapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // 🌟 CORREÇÃO: Buscando pelo 'accountHolder' da conta vinculada à transação
    @Query("SELECT t FROM Transaction t WHERE t.account.accountHolder.id = :userId ORDER BY t.paymentDate DESC")
    List<Transaction> findAllByUserIdOrderByPaymentDateDesc(@Param("userId") String userId);

    boolean existsByReversalOfId(UUID originalTransactionId);

    // 🌟 CORREÇÃO: Aplicando a mesma regra na busca por ID para o estorno
    @Query("SELECT t FROM Transaction t WHERE t.id = :id AND t.account.accountHolder.id = :userId")
    Optional<Transaction> findByIdAndUserId(@Param("id") UUID id, @Param("userId") String userId);
}