package com.project.financeapi.dto.OperationType;

import com.project.financeapi.enums.MovementType;

import java.util.UUID;

public record OperationTypeRequestUpdateDTO(
        String name,
        MovementType movementType,
        UUID operationGroupId
) {
}
