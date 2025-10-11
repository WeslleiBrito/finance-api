package com.project.financeapi.dto.OperationType;

import com.project.financeapi.enums.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record OperationTypeRequestCreateDTO(
        @NotNull(message = "O nome deve ser informado.")
        @Size(min = 3, max = 80, message = "O nome precisa ter de 3 a 80 caracteres.")
        String name,
        @NotNull(message = "O tipo de movimento é obrigatório.")
        MovementType movementType,
        @NotNull(message = "É obrigatório informar o grupo de operação a que pertence o tipo de operação.")
        @Size(min = 36, max = 36, message = "O id do tipo do grupo de operação é inválido.")
        String operationGroupId
) {
}
