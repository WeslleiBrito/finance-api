package com.project.financeapi.dto.card.creditCard;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


public record CreditCardCreateRequestDTO(
        @NotNull(message = "O nome do cartão é obrigatório")
        String name,
        @DecimalMin(value = "0.01", inclusive = true, message = "O valor mínimo do limite é 0,01 centavos.")
        BigDecimal creditLimit,
        @Min(value = 1, message = "A menor dia para o fechamento do cartão é 1.")
        @Max(value = 31, message = "A maior dia para o fechamento do cartão é 31.")
        Integer closingDay,
        @Min(value = 1, message = "A menor data para o vencimento do cartão é 1.")
        @Max(value = 31, message = "A maior data para o vencimento do cartão é 31.")
        Integer dueDay,
        @NotNull(message = "A bandeira precisa ser informada.")
        String cardBrand,
        String bank,
        BigDecimal revolvingInterest,
        BigDecimal fine

) {
}
