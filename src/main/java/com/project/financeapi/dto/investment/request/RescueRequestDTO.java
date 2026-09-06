package com.project.financeapi.dto.investment.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Solicitação de retirada. O Front-end pede apenas o valor;
 * o Back-end se vira com o algoritmo PEPS e o corte vertical.
 */
public record RescueRequestDTO(
        @NotNull @DecimalMin("0.01") BigDecimal requestedAmount
) {}