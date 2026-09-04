package com.project.financeapi.dto.investment;

import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentApportDTO(
        // Se for nulo, cria uma sacola nova. Se vier preenchido, é um novo aporte em sacola existente.
        UUID fixedIncomeId,

        @NotNull(message = "A conta de origem é obrigatória")
        UUID accountId,

        // Dados do contrato (Obrigatórios apenas se fixedIncomeId for nulo)
        String name,
        FixedIncomeType type,
        IndexerType indexer,
        BigDecimal contractedRate,
        LocalDate maturityDate,

        @NotNull(message = "O valor do aporte é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal amount,

        @NotNull(message = "A data do aporte é obrigatória")
        LocalDate purchaseDate
) {}