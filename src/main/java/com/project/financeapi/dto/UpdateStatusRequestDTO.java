package com.project.financeapi.dto;

import com.project.financeapi.enums.OperationStatus;

public record UpdateStatusRequestDTO(
        OperationStatus operationStatus
) {
}
