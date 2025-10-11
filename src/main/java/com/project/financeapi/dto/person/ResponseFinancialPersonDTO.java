package com.project.financeapi.dto.person;

import com.project.financeapi.dto.document.DocumentResponseDTO;
import com.project.financeapi.dto.transaction.TransactionResponseDTO;

import java.util.List;

public record ResponseFinancialPersonDTO(
        List<DocumentResponseDTO> documents
) {
}
