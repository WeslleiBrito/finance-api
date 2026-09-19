package com.project.financeapi.dto.investment.request;

import java.util.UUID;

public record CreateBoxDTO(
        UUID productId, // ID do produto criado no passo 1
        String name     // Ex: "Cofrinho", "Reserva de Emergência"
) {}