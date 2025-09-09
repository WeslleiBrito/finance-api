package com.project.financeapi.dto.email;

import jakarta.validation.Valid;

import java.util.List;

public record EmailListDTO(
        @Valid
        List<EmailDTO> emails
) {
}
