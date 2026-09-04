package com.project.financeapi.dto.OperationType;

import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.enumSystem.MovementType;
import com.project.financeapi.enumSystem.StatusEntity;

import java.util.UUID;

public record OperationTypeResponseDTO(
        UUID id,
        String name,
        MovementType movementType,
        StatusEntity statusEntity,
        Boolean isSystem,
        OperationGroupResponseDTO operationGroup
) {
}
