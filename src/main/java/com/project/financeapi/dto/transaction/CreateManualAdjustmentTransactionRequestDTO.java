package com.project.financeapi.dto.transaction;

import java.util.List;

public record CreateManualAdjustmentTransactionRequestDTO(
        List<CreateManualAdjustmentTransactionDTO> dto
) {
}
