package com.project.financeapi.dto.Installment;

import com.project.financeapi.enums.MovementType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateInstallmentDTO(
        @NotNull(message = "O id do documento precisa ser informado.")
        @NotBlank(message = "O id não pode ser vazio;")
        UUID documentId,

        @NotNull(message = "O tipo de movimento não foi informado.")
        MovementType movementType,

        @NotNull(message = "As parcelas devem ser informadas")
        List<@Valid InstallmentDTO> installments
) {
}
