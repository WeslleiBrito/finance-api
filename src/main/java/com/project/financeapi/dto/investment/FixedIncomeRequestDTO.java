package com.project.financeapi.dto.investment;

import com.project.financeapi.enumSystem.FixedIncomeType;
import com.project.financeapi.enumSystem.IndexerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FixedIncomeRequestDTO(
        @NotBlank(message = "O nome do investimento é obrigatório")
        String name,

        @NotNull(message = "O tipo do ativo é obrigatório")
        FixedIncomeType type,

        @NotNull(message = "O indexador é obrigatório")
        IndexerType indexer,

        @NotNull(message = "A taxa contratada é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "A taxa deve ser maior que zero")
        BigDecimal contractedRate,

        @NotNull(message = "O valor investido é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "O valor investido deve ser maior que zero")
        BigDecimal principalAmount,

        @NotNull(message = "A data de compra é obrigatória")
        LocalDate purchaseDate,

        @NotNull(message = "A data de vencimento é obrigatória")
        LocalDate maturityDate
) {}