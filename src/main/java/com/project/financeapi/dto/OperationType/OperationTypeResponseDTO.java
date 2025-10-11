package com.project.financeapi.dto.OperationType;

import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.OperationStatus;

public record OperationTypeResponseDTO(
        String id,
        String name,
        MovementType movementType,
        OperationStatus operationStatus,
        Boolean isGlobal,
        OperationGroupResponseDTO operationGroup
) {
}
