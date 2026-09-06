package com.project.financeapi.dto.integration;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BacenSgsResponseDTO(
        // O Bacen devolve a data no padrão brasileiro
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate data,

        BigDecimal valor
) {}