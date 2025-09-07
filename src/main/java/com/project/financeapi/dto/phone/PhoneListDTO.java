package com.project.financeapi.dto.phone;
import jakarta.validation.Valid;

import java.util.List;

public record PhoneListDTO(
        @Valid
        List<PhoneDTO> phones
) {}
