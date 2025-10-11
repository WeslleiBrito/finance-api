package com.project.financeapi.dto.operationGroup;

import com.project.financeapi.enums.OperationStatus;

public record OperationGroupResponseDTO(
        String id,
        String name,
        Boolean isGlobal,
        OperationStatus operationStatus
) {
}
