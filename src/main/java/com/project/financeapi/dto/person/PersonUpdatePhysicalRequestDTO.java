package com.project.financeapi.dto.person;

import com.project.financeapi.dto.address.AddressDTO;
import com.project.financeapi.dto.email.EmailDTO;
import com.project.financeapi.dto.phone.PhoneDTO;
import com.project.financeapi.enumSystem.PersonRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PersonUpdatePhysicalRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotNull(message = "O papel (role) é obrigatório.")
        PersonRole role,

        String nickname,
        String CPF,

        // Recebe os DTOs de entrada simples (sem ID)
        @NotNull(message = "A lista de telefones não pode ser nula.")
        List<PhoneDTO> phoneList,

        @NotNull(message = "A lista de e-mails não pode ser nula.")
        List<EmailDTO> emailList,

        @NotNull(message = "A lista de endereços não pode ser nula.")
        List<AddressDTO> addressesList
) {}