package com.project.financeapi.dto;

import com.project.financeapi.enumSystem.StatusEntity;

public record UpdateStatusRequestDTO(
        StatusEntity statusEntity
) {
}
