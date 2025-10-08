package com.project.financeapi.dto.person;

import com.project.financeapi.dto.address.ResponseAddressDTO;
import com.project.financeapi.dto.email.ResponseEmailDTO;
import com.project.financeapi.dto.phone.ResponsePhoneDTO;
import com.project.financeapi.enums.PersonType;

import java.util.List;

public record ResponsePersonDTO(
        String id,
        String name,
        PersonType personType,
        String cpf,
        String cnpj,
        List<ResponsePhoneDTO> phones,
        List<ResponseEmailDTO> emails,
        List<ResponseAddressDTO> addresses,
        ResponseFinancialPersonDTO financialPerson
) {
}
