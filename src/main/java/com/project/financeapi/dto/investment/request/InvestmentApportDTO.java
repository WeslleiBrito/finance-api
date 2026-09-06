package com.project.financeapi.dto.investment.request;

import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentApportDTO(
        // Identificação
        @NotNull(message = "A conta bancária de origem é obrigatória.")
        UUID accountId,

        // Se null, cria um novo contrato. Se preenchido, adiciona lote a um existente.
        UUID fixedIncomeId,

        // Dados do Papel (Obrigatórios apenas se fixedIncomeId for null)
        String name,
        FixedIncomeType type, // CDB, LCI, etc. (Substituiu o isTaxExempt)
        IndexerType indexer,
        @DecimalMin(value = "0.01", message = "A taxa contratada deve ser maior que zero.")
        BigDecimal contractedRate,
        LocalDate maturityDate,

        // Dados do Lote (Tranche)
        LocalDate purchaseDate, // Pode ser retroativa (Data Fiscal)

        @NotNull(message = "O valor do aporte é obrigatório.")
        @Positive(message = "O valor do aporte deve ser positivo.")
        BigDecimal amount
) {}