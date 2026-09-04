package com.project.financeapi.dto.integration;

import java.time.LocalDate;

public record OfficialHolidayDTO(
        LocalDate date,
        String name,
        String type
) {}