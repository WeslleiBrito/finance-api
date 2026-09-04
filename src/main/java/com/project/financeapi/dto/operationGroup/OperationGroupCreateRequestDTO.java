package com.project.financeapi.dto.operationGroup;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OperationGroupCreateRequestDTO(
        @NotNull(message = "O nome é obrigatório")
        @Size(min = 3, max = 80, message = "O nome precisa ter pelo menos 3 caracteres e no máximo 80.")
        String name
) {
}
