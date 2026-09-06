package com.project.financeapi.repository;

import com.project.financeapi.entity.InvestmentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, UUID> {
    // Fica VAZIO! Toda a carga de "Último rendimento" e "Saldo Base"
    // Foi totalmente migrada para dentro do Aggregate Root (FixedIncomeLot).
}