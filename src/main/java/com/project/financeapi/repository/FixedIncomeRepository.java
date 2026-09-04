package com.project.financeapi.repository;

import com.project.financeapi.entity.FixedIncome;
import com.project.financeapi.enumSystem.FixedIncomeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FixedIncomeRepository extends JpaRepository<FixedIncome, UUID> {

    // Busca todas as sacolas (ativas ou fechadas) de uma conta específica
    List<FixedIncome> findAllByAccountId(UUID accountId);

    // Busca apenas as sacolas ativas de uma conta (útil para o dashboard principal)
    List<FixedIncome> findAllByAccountIdAndStatus(UUID accountId, FixedIncomeStatus status);
}