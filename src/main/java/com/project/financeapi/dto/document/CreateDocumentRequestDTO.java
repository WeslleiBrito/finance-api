package com.project.financeapi.dto.document;

import com.project.financeapi.dto.Installment.InstallmentDTO;
import com.project.financeapi.enums.MovementType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateDocumentRequestDTO(
        @NotNull(message = "Defina o tipo de movimento.")
        MovementType movementType,

        @NotNull(message = "A descrição é obrigatória.")
        @Size(min = 3, max = 20, message = "A descrição deve ter de 3 a 20 caracteres.")
        String description,

        @NotNull(message = "O valor total do documento precisa ser informado.")
        @Positive(message = "O valor total do documento precisa ser maior que zero.")
        BigDecimal totalAmount,

        @NotNull(message = "A pessoa beneficiária/fornecedora não foi informada.")
        String personId,

        @NotNull(message = "A conta não foi informada.")
        String accountId,

        @NotNull(message = "As parcelas devem ser informadas")
        List<@Valid InstallmentDTO> installments
) {
}
