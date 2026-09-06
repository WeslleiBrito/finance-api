package com.project.financeapi.dto.investment.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Visão detalhada de cada Tranche.
 * Agora contém o extrato imutável aninhado de tudo que aconteceu com este lote específico.
 */
public record LotDetailDTO(
        UUID id,
        LocalDate purchaseDate,
        long ageInDays,
        BigDecimal remainingPrincipal,
        BigDecimal projectedGrossBalance,
        BigDecimal currentIrTaxProvision,
        BigDecimal currentIofTaxProvision,
        BigDecimal projectedNetBalance,
        List<TransactionLedgerDTO> transactions // <-- HIERARQUIA RESTAURADA
) {}