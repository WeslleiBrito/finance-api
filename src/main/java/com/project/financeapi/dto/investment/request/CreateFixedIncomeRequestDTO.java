package com.project.financeapi.dto.investment.request;

import com.project.financeapi.enumSystem.IndexerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Criação do Contrato (Papel) e do Lote Inicial (Marco Zero).
 */
public record CreateFixedIncomeRequestDTO(
        @NotNull UUID accountId,
        @NotBlank String name, // Ex: "CDB Banco Inter"
        @NotNull IndexerType indexer, // CDI, IPCA, PREFIXED
        @NotNull @DecimalMin("0.01") BigDecimal contractedRate, // Ex: 110.0 (110% do CDI)
        @NotNull Boolean isTaxExempt, // LCI/LCA = true
        @NotNull LocalDate maturityDate, // Vencimento do contrato

        // Dados exclusivos do primeiro lote (Tranche)
        @NotNull LocalDate purchaseDate, // Data fiscal (para IR/IOF). Se for legado, vem com data antiga.
        @NotNull @DecimalMin("0.01") BigDecimal initialAmount // Valor bruto do aporte
) {
    // Nota arquitetural: O ledgerStartDate (Data Financeira) não vem do Front-end.
    // O Back-end injetará LocalDate.now() no momento da criação para travar o motor de rendimentos.
}