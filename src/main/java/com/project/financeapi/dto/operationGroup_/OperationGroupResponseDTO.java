package com.project.financeapi.dto.operationGroup_;

import com.project.financeapi.enumSystem.StatusEntity;

import java.util.UUID;

public record OperationGroupResponseDTO(
        UUID id,
        String name,
        Boolean isSystem,
        StatusEntity status
) {
}
