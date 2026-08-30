package com.project.financeapi.util.mapper.bank;
import com.project.financeapi.dto.bank.*;
import com.project.financeapi.entity.Bank;

public class BankMapperResponse {

    private BankMapperResponse() {}

    public static BankResponseDTO toResponse(Bank bank) {
        return new BankResponseDTO(
                bank.getId(),
                bank.getName(),
                bank.getCode(),
                bank.getStatus()
        );
    }

}
