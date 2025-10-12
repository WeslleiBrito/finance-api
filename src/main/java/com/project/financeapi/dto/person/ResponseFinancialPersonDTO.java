package com.project.financeapi.dto.person;

import com.project.financeapi.dto.invoice.InvoiceResponseDTO;

import java.util.List;

public record ResponseFinancialPersonDTO(
        List<InvoiceResponseDTO> documents
) {
}
