package com.project.financeapi.dto.investment.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record InvestmentRescueDTO(
        @NotNull(message = "O ID do investimento é obrigatório.")
        UUID boxId,

        @NotNull(message = "O valor solicitado para resgate é obrigatório.")
        @Positive(message = "O valor do resgate deve ser positivo.")
        BigDecimal requestedAmount
) {}