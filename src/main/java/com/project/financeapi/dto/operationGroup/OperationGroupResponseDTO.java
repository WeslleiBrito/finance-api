package com.project.financeapi.dto.operationGroup;

import com.project.financeapi.enums.OperationStatus;

import java.util.UUID;

public record OperationGroupResponseDTO(
        UUID id,
        String name,
        Boolean isGlobal,
        OperationStatus operationStatus
) {
}
