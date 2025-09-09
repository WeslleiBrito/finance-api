package com.project.financeapi.dto.transaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


import java.util.List;

public record TransactionRequestDTO(

        @NotNull(message = "O id da conta de operação é obrigatório.")
        String accountId,

        @NotNull(message = "A lista de transações não pode ser nula.")
        @Size(min = 1, message = "É necessário informar pelo menos uma transação.")
        List<@Valid TransactionDTO> transactions
) {}
