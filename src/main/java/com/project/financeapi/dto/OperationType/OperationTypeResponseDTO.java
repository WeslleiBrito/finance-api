package com.project.financeapi.dto.OperationType;

import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.enums.MovementType;
import com.project.financeapi.enums.OperationStatus;

import java.util.UUID;

public record OperationTypeResponseDTO(
        UUID id,
        String name,
        MovementType movementType,
        OperationStatus operationStatus,
        Boolean isGlobal,
        OperationGroupResponseDTO operationGroup
) {
}
