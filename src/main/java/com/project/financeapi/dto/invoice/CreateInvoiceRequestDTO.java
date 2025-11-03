package com.project.financeapi.dto.invoice;

import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.enums.MovementType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateInvoiceRequestDTO(

        @NotNull(message = "O tipo de operação não foi informado.")
        UUID operationTypeId,

        @NotNull(message = "O valor total do documento precisa ser informado.")
        @Positive(message = "O valor total do documento precisa ser maior que zero.")
        BigDecimal totalAmount,

        @NotNull(message = "A pessoa beneficiária/fornecedora não foi informada.")
        UUID personId,

        @NotNull(message = "A conta não foi informada.")
        UUID accountId,

        @NotNull(message = "As parcelas devem ser informadas")
        List<@Valid InstallmentDTO> installments
) {
}
