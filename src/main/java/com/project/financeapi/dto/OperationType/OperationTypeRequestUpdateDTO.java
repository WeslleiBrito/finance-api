package com.project.financeapi.dto.OperationType;

import com.project.financeapi.enums.MovementType;

public record OperationTypeRequestUpdateDTO(
        String name,
        MovementType movementType,
        String operationGroupId
) {
}
