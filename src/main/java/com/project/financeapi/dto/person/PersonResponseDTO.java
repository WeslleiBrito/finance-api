package com.project.financeapi.dto.person;

import com.project.financeapi.dto.address.ResponseAddressDTO;
import com.project.financeapi.dto.email.ResponseEmailDTO;
import com.project.financeapi.dto.invoice.InvoiceResponseDTO;
import com.project.financeapi.dto.phone.ResponsePhoneDTO;
import com.project.financeapi.enumSystem.PersonRole;
import com.project.financeapi.enumSystem.PersonType;

import java.util.List;
import java.util.UUID;

public interface PersonResponseDTO {
    UUID id();
    String name();
    PersonType personType();
    PersonRole role();
    List<ResponsePhoneDTO> phones();
    List<ResponseEmailDTO> emails();
    List<ResponseAddressDTO> addresses();
    List<InvoiceResponseDTO> invoices();
}
