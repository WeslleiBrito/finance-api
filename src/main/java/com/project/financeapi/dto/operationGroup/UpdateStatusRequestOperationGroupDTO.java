package com.project.financeapi.dto.operationGroup;

import com.project.financeapi.enums.OperationStatus;

public record UpdateStatusRequestOperationGroupDTO(
        OperationStatus operationStatus
) {
}
