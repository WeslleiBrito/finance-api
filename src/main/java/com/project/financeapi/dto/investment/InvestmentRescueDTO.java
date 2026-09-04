package com.project.financeapi.dto.investment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentRescueDTO(
        @NotNull(message = "O ID do investimento é obrigatório")
        UUID fixedIncomeId,

        @NotNull(message = "O valor do resgate é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor do resgate deve ser maior que zero")
        BigDecimal rescueAmount,

        @NotNull(message = "A data do resgate é obrigatória")
        LocalDate rescueDate
) {}