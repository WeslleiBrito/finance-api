package com.project.financeapi.dto.person;

import com.project.financeapi.dto.address.AddressDTO;
import com.project.financeapi.dto.email.EmailDTO;
import com.project.financeapi.dto.phone.PhoneDTO;
import com.project.financeapi.enumSystem.PersonRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PersonUpdateLegalRequestDTO(
        @NotBlank(message = "A razão social é obrigatória.")
        String name,

        @NotNull(message = "O papel (role) é obrigatório.")
        PersonRole role,

        String tradeName,
        String CNPJ,

        @NotNull(message = "A lista de telefones não pode ser nula.")
        List<PhoneDTO> phoneList,

        @NotNull(message = "A lista de e-mails não pode ser nula.")
        List<EmailDTO> emailList,

        @NotNull(message = "A lista de endereços não pode ser nula.")
        List<AddressDTO> addressesList
) {}