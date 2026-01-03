package com.project.financeapi.dto.person;

import com.project.financeapi.dto.address.AddressDTO;
import com.project.financeapi.dto.email.EmailDTO;
import com.project.financeapi.dto.phone.PhoneDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PersonCreatePhysicalRequestDTO(
        @NotNull(message = "O nome precisa ser informado.")
        @Size(min = 3, message = "O nome precisa ter pelo menos 3 caracteres.")
        String name,
        @NotNull(message = "CPF não informado")
        String CPF,
        String nickname,
        List<@Valid AddressDTO> addressesList,
        List<@Valid PhoneDTO> phoneList,
        List<@Valid EmailDTO> emailList
) {
}
