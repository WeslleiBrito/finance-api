package com.project.financeapi.dto.investment.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvestmentApportDTO(
        UUID accountId, // Conta de onde o dinheiro será debitado para o aporte
        UUID boxId,     // Destino final do dinheiro (ID da meta criada no passo 2)
        BigDecimal amount,
        LocalDate purchaseDate
) {}