package com.project.financeapi.dto.account;

import com.project.financeapi.dto.user.ResponseUserDTO;
import com.project.financeapi.enums.AccountStatus;
import com.project.financeapi.enums.AccountType;

import java.math.BigDecimal;


public record ResponseAccountDTO (
  String id,
  String name,
  AccountType type,
  BigDecimal balance,
  AccountStatus status,
  ResponseUserDTO accountHolder
) {

}
