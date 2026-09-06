package com.project.financeapi.dto.investment.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Novo aporte em um contrato já existente (Criação de um novo Lote/Tranche).
 */
public record AddLotRequestDTO(
        @NotNull LocalDate purchaseDate,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {}