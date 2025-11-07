package com.project.financeapi.dto;

import com.project.financeapi.entity.Bank;
import com.project.financeapi.entity.User;

public record ResponseValidateDTO(
        User user,
        Bank bank
) {
}
