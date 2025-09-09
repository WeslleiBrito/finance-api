package com.project.financeapi.dto.account;

import com.project.financeapi.dto.user.ResponseUserDTO;
import com.project.financeapi.entity.Address;
import com.project.financeapi.entity.Email;
import com.project.financeapi.entity.Phone;

import java.util.List;

public record ResponseAccountDTO (
  String id,
  String name,
  String personType,
  String tradeName,
  String cnpj,
  String nickname,
  String cpf,
  ResponseUserDTO createdBy,
  List<Phone> phones,
  List<Email> emails,
  List<Address> addresses

) {

}
